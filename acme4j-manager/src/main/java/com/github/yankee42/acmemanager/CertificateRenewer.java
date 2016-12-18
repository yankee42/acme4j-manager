package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.RegistrationBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.exception.AcmeConflictException;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeUnauthorizedException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Creates and signs certificates or renews alread existing certificates.
 */
public class CertificateRenewer {

    private static final int KEY_SIZE = 2048;
    private static final Logger log = LoggerFactory.getLogger(CertificateRenewer.class);

    private final String acmeEndpoint;
    private final UserKeyRepository userKeyRepository;
    private final TosAcceptor tosAcceptor;
    private final ChallengeFactory challengeFactory;


    public CertificateRenewer(final String acmeEndpoint,
                              final UserKeyRepository userKeyRepository,
                              final TosAcceptor tosAcceptor,
                              final ChallengeFactory challengeFactory) {
        this.acmeEndpoint = acmeEndpoint;
        this.userKeyRepository = userKeyRepository;
        this.tosAcceptor = tosAcceptor;
        this.challengeFactory = challengeFactory;
    }

    public void renewCertificates(final Collection<CertificateRecord> recordsToRenew) throws IOException, AcmeException {
        for (final CertificateRecord certificateRecord : recordsToRenew) {
            renewCertificate(certificateRecord);
        }
    }

    public void renewCertificate(final CertificateRecord certificateRecord) throws IOException, AcmeException {
        final Collection<String> domains = certificateRecord.getDomains();
        log.info("Renewing certificates for {}", domains);

        final Registration reg = getOrCreateUserKeyPair();
        final URI agreement = reg.getAgreement();

        for (String domain : domains) {
            log.info("Authorizing domain {}", domain);
            final Authorization auth = authorizeDomain(reg, agreement, domain);
            final Challenge challenge = challengeFactory.createChallenge(auth, domain);

            if (challenge == null) {
                return;
            }

            challenge.trigger();
            waitForChallengeComplete(challenge);
        }
        log.info("all domains successfully authorized");

        KeyPair domainKeyPair;
        try (Reader domainKeyReader = certificateRecord.getDomainKey().getAsReader()) {
            if (domainKeyReader == null) {
                log.info("No keypair exists for {}. Creating...", domains);
                domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
                certificateRecord.setDomainKeyPair(new KeyPairWriterWriter(domainKeyPair));
            } else {
                log.info("Using existing keyPair for {}", domains);
                domainKeyPair = KeyPairUtils.readKeyPair(domainKeyReader);
            }
        }

        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomains(domains);
        csrb.sign(domainKeyPair);

        log.info("Requesting certificate");
        Certificate certificate = reg.requestCertificate(csrb.getEncoded());
        log.info("Downloading certificates");

        X509Certificate cert = certificate.download();
        X509Certificate[] chain = certificate.downloadChain();

        log.info("Saving certificates");
        certificateRecord.setCertificate(new X509CertificateWriterWriter(cert));
        certificateRecord.setCertificateChain(new X509CertificateChainWriterWriter(chain));
        certificateRecord.setExpires(cert.getNotAfter().toInstant());
        certificateRecord.flush();

        log.info("certificate renewal completed");
    }

    private Registration getOrCreateUserKeyPair() throws IOException, AcmeException {
        boolean createdNewKeyPair = false;

        KeyPair userKeyPair;
        try (Reader userKeyReader = userKeyRepository.getUserKey()) {
            if (userKeyReader == null) {
                log.info("No user key pair exists. Creating.");
                userKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
                userKeyRepository.saveUserKey(new KeyPairWriterWriter(userKeyPair));
                createdNewKeyPair = true;
            } else {
                log.info("Using existing user key pair");
                userKeyPair = KeyPairUtils.readKeyPair(userKeyReader);
            }
        }
        Registration reg = register(userKeyPair);

        URI agreement = reg.getAgreement();

        if (createdNewKeyPair) {
            tryAcceptTos(reg, agreement);
        }
        return reg;
    }

    private void tryAcceptTos(final Registration reg, final URI agreement) throws AcmeException {
        tosAcceptor.tryAcceptTos(reg, agreement);
        reg.modify().setAgreement(agreement).commit();
    }

    private void waitForChallengeComplete(final Challenge challenge) throws AcmeException {
        try {
            int attempts = 10;
            while (true) {
                if (challenge.getStatus() == Status.VALID) {
                    return;
                }
                if (challenge.getStatus() == Status.INVALID) {
                    throw new ChallengeFailedException();
                }
                if (attempts == 0) {
                    throw new ChallengeTimedOutException();
                }
                Thread.sleep(3000L);
                challenge.update();
                --attempts;
            }
        } catch (InterruptedException ex) {
            throw new Error(ex);
        }
    }

    private Authorization authorizeDomain(final Registration reg, final URI agreement, final String domain) throws AcmeException {
        try {
            return reg.authorizeDomain(domain);
        } catch (AcmeUnauthorizedException ex) {
            // Maybe there are new T&C to accept?
            log.trace("acme4j exception caught", ex);
            tryAcceptTos(reg, agreement);

            // Then try again...
            return reg.authorizeDomain(domain);
        }
    }

    private Registration register(final KeyPair userKeyPair) throws AcmeException {
        Session session = new Session(acmeEndpoint, userKeyPair);

        // Register a new user
        Registration reg;
        try {
            reg = new RegistrationBuilder().create(session);
        } catch (AcmeConflictException ex) {
            log.trace("acme4j exception caught", ex);
            reg = Registration.bind(session, ex.getLocation());
            log.info("Account does already exist, URI: " + reg.getLocation());
        }
        return reg;
    }
}

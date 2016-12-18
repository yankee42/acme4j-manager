package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmedeployheroku.HerokoApi;
import com.github.yankee42.acmedeployheroku.SniEndpointCreateUpdatePayload;
import com.github.yankee42.acmedeployheroku.SniEndpointListPayload;
import com.github.yankee42.acmemanager.CertificateRecord;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Deploys certificates to Heroku
 */
public class HerokuDeploy {
    private final String herokuToken;
    private final Function<String, String> domainToAppName;

    /**
     *
     * @param herokuToken an API access token to authenticate against the Heroku API
     * @param domainToAppName a function that derives the app name from the domain
     */
    public HerokuDeploy(final String herokuToken, final Function<String, String> domainToAppName) {
        this.herokuToken = herokuToken;
        this.domainToAppName = domainToAppName;
    }

    public void deploy(final CertificateRecord certificate) throws IOException {
        final String appName = domainToAppName.apply(certificate.getDomains().get(0));
        final String certificateChain =
            certificate.getCertificate().getAsString() + certificate.getCertificateChain().getAsString();
        final String privateKey = certificate.getDomainKey().getAsString();
        final SniEndpointCreateUpdatePayload createUpdatePayload =
            new SniEndpointCreateUpdatePayload(certificateChain, privateKey);

        final HerokoApi herokoApi = new HerokoApi(herokuToken);
        final List<SniEndpointListPayload> sniEndpointList = herokoApi.listHerokuSniEndpoints(appName);
        if (sniEndpointList.isEmpty()) {
            herokoApi.createHerokuSniEndpoint(appName, createUpdatePayload);
        } else {
            herokoApi.updateHerokuSslEndpoint(appName, sniEndpointList.get(0).getId(), createUpdatePayload);
        }
    }
}

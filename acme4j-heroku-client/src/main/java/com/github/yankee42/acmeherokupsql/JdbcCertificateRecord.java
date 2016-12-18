package com.github.yankee42.acmeherokupsql;

import com.github.yankee42.acmemanager.CertificateRecord;
import com.github.yankee42.acmemanager.CertificateResource;
import com.github.yankee42.acmemanager.CertificateResourceString;
import com.github.yankee42.acmemanager.WriterWriter;
import com.github.yankee42.simplejdbc.jdbc.JdbcQuery;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcCertificateRecord implements CertificateRecord {
    private final DataSource dataSource;
    private int id;
    private String domain;
    private List<String> aliases;
    private Instant exires;
    private String keyPair;
    private String certificate;
    private String certificateChain;
    private boolean inserted = false;

    JdbcCertificateRecord(final DataSource dataSource) {
        this.dataSource = dataSource;
        aliases = new ArrayList<>();
    }

    JdbcCertificateRecord(final DataSource dataSource,
                          final int id,
                          final String domain,
                          final ArrayList<String> aliases,
                          final Instant expires,
                          final String keyPair,
                          final String certificate,
                          final String certificateChain) {
        this.dataSource = dataSource;
        this.id = id;
        this.domain = domain;
        this.aliases = aliases;
        this.exires = expires;
        this.keyPair = keyPair;
        this.certificate = certificate;
        this.certificateChain = certificateChain;
    }

    @Override
    public CertificateResource getDomainKey() {
        return certificateResourceIfHasContent(keyPair);
    }

    @Override
    public CertificateResource getCertificate() {
        return certificateResourceIfHasContent(certificate);
    }

    @Override
    public CertificateResource getCertificateChain() {
        return certificateResourceIfHasContent(certificateChain);
    }

    private CertificateResourceString certificateResourceIfHasContent(final String txt) {
        if (txt == null || txt.isEmpty()) {
            return new CertificateResourceString(null);
        }
        return new CertificateResourceString(txt);
    }

    @Override
    public void setDomainKeyPair(final WriterWriter writerWriter) {
        keyPair = WriterWriterToString.writeToString(writerWriter);
    }

    @Override
    public void setCertificate(final WriterWriter writerWriter) {
        certificate = WriterWriterToString.writeToString(writerWriter);
    }

    @Override
    public void setCertificateChain(final WriterWriter writerWriter) {
        certificateChain = WriterWriterToString.writeToString(writerWriter);
    }

    @Override
    public void setExpires(final Instant expires) {
        this.exires = expires;
    }

    @Override
    public List<String> getDomains() {
        final List<String> result = new ArrayList<>(aliases.size() + 1);
        result.add(domain);
        result.addAll(aliases);
        return result;
    }

    public void flush() {
        if (inserted) {
            throw new IllegalStateException("Cannot update a certificate that has been inserted just before, because generated key extraction is not implemented, yet.");
        }
        try {
            tryPersist();
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    private void tryPersist() throws SQLException {
        if (id == 0) {
            JdbcQuery.forPreparedSql(
                "insert into certificates (domain,aliases,expires,keyPair,certificate,certificateChain) values (?,?,?,?,?,?)",
                this::setBasicSaveParams
            ).update(dataSource);
            inserted = true;
        } else {
            JdbcQuery.forPreparedSql(
                "update certificates set domain=?,aliases=?,expires=?,keyPair=?,certificate=?,certificateChain=? where id=?",
                stmt -> {
                    setBasicSaveParams(stmt);
                    stmt.setInt(7, id);
                }
            ).update(dataSource);
        }
    }

    private void setBasicSaveParams(final PreparedStatement stmt) throws SQLException {
        stmt.setString(1, domain);
        stmt.setString(2, aliases.stream().collect(Collectors.joining(",")));
        stmt.setTimestamp(3, Timestamp.from(exires));
        stmt.setString(4, keyPair);
        stmt.setString(5, certificate);
        stmt.setString(6, certificateChain);
    }

}

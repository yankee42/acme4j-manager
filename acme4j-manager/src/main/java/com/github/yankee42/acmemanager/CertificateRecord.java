package com.github.yankee42.acmemanager;

import java.time.Instant;
import java.util.List;

public interface CertificateRecord {
    CertificateResource getDomainKey();
    CertificateResource getCertificate();
    CertificateResource getCertificateChain();

    void setDomainKeyPair(WriterWriter writerWriter);
    void setCertificate(WriterWriter writerWriter);
    void setCertificateChain(WriterWriter writerWriter);
    void setExpires(Instant expires);

    List<String> getDomains();

    /**
     * For optimization reasons, if some data is refreshed using the setters if this class the saved certificates might
     * not be propagated to storage backend (database, filesystem,...) before this method is called.
     */
    void flush();
}

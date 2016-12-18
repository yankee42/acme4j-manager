package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.util.CertificateUtils;

import java.io.IOException;
import java.io.Writer;
import java.security.cert.X509Certificate;

public class X509CertificateWriterWriter implements WriterWriter {
    private final X509Certificate certificate;

    public X509CertificateWriterWriter(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    @Override
    public void write(final Writer writer) throws IOException {
        CertificateUtils.writeX509Certificate(certificate, writer);
    }
}

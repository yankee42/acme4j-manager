package com.github.yankee42.acmemanager;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.Writer;
import java.security.cert.X509Certificate;

public class X509CertificateChainWriterWriter implements WriterWriter {
    private final X509Certificate[] chain;

    public X509CertificateChainWriterWriter(final X509Certificate[] chain) {
        this.chain = chain;
    }

    @Override
    public void write(final Writer writer) throws IOException {
        try (JcaPEMWriter jw = new JcaPEMWriter(writer)) {
            for (X509Certificate c : chain) {
                if (c != null) {
                    jw.writeObject(c);
                }
            }
        }
    }
}

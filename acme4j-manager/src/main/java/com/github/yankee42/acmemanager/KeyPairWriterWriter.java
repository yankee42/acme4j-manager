package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.IOException;
import java.io.Writer;
import java.security.KeyPair;

public class KeyPairWriterWriter implements WriterWriter {
    private final KeyPair keyPair;

    public KeyPairWriterWriter(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public void write(final Writer writer) throws IOException {
        KeyPairUtils.writeKeyPair(keyPair, writer);
    }
}

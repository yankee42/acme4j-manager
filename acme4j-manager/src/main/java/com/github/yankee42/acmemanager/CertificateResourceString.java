package com.github.yankee42.acmemanager;

import java.io.Reader;
import java.io.StringReader;

public class CertificateResourceString implements CertificateResource {
    private final String resource;

    public CertificateResourceString(final String resource) {
        this.resource = resource;
    }

    @Override
    public Reader getAsReader() {
        if (resource == null) {
            return null;
        }
        return new StringReader(resource);
    }

    @Override
    public String getAsString() {
        return resource;
    }
}

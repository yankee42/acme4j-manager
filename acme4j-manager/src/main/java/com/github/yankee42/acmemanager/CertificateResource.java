package com.github.yankee42.acmemanager;

import java.io.Reader;

public interface CertificateResource {
    /**
     * @return The certificate as a reader. May be null if the resource does not exist.
     */
    Reader getAsReader();
    /**
     * @return The certificate as a string. May be null if the resource does not exist.
     */
    String getAsString();
}

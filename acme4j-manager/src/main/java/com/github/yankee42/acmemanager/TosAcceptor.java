package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.Registration;

import java.net.URI;

public interface TosAcceptor {
    /**
     * Present the TOS to the user and aks the user to accept. Do nothing if the user accepts, throw a
     * {@link TosDeclinedException} if the user declines.
     *
     * @param reg
     * @param agreement
     * @throws TosDeclinedException if the user declined the TOS
     */
    void tryAcceptTos(Registration reg, URI agreement) throws TosDeclinedException;
}

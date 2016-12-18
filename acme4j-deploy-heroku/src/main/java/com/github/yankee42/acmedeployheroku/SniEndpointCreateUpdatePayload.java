package com.github.yankee42.acmedeployheroku;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SniEndpointCreateUpdatePayload {
    @JsonProperty("certificate_chain")
    private String certificateChain;
    @JsonProperty("private_key")
    private String privateKey;

    public SniEndpointCreateUpdatePayload() {
    }

    public SniEndpointCreateUpdatePayload(final String certificateChain, final String privateKey) {
        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
    }

    public String getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(final String certificateChain) {
        this.certificateChain = certificateChain;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }
}

package com.github.yankee42.acmedeployheroku;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SniEndpointListPayload {
    private String id;
    private String name;
    private String cname;

    @JsonProperty("certificate_chain")
    private String certificateChain;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(final String cname) {
        this.cname = cname;
    }

    public String getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(final String certificateChain) {
        this.certificateChain = certificateChain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}

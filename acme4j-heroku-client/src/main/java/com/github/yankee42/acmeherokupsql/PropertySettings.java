package com.github.yankee42.acmeherokupsql;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public class PropertySettings implements Settings {
    private final Properties properties;

    public PropertySettings(final Properties properties) {
        this.properties = properties;
    }

    public static Settings load(final InputStream in) throws IOException {
        Objects.requireNonNull(in, "settings input stream");
        final Properties properties = new Properties();
        properties.load(in);
        return new PropertySettings(properties);
    }

    @Override
    public String getHerokuToken() {
        return properties.getProperty("heroku_token");
    }

    @Override
    public String getDbUrl() {
        return properties.getProperty("db_url");
    }

    @Override
    public String getDbUser() {
        return properties.getProperty("db_user");
    }

    @Override
    public String getDbPassword() {
        return properties.getProperty("db_password");
    }

    @Override
    public String getTokenDeployPsk() {
        return properties.getProperty("token_deploy_psk");
    }

    @Override
    public Pattern getDomainToAppNameRegex() {
        return Pattern.compile(properties.getProperty("domain_to_app_name_regex"));
    }

    @Override
    public String getDomainToAppNameReplace() {
        return properties.getProperty("domain_to_app_name_replace");
    }

    @Override
    public String getAcmeEndpoint() {
        return properties.getProperty("acme_endpoint");
    }
}

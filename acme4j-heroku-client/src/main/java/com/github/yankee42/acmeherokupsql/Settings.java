package com.github.yankee42.acmeherokupsql;

import java.util.regex.Pattern;

public interface Settings {
    String getHerokuToken();
    String getDbUrl();
    String getDbUser();
    String getDbPassword();
    String getTokenDeployPsk();
    Pattern getDomainToAppNameRegex();
    String getDomainToAppNameReplace();
    String getAcmeEndpoint();
}

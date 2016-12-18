package com.github.yankee42.acmeherokupsql;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexDomainToAppName implements Function<String, String> {
    private final Pattern domainToAppNameRegex;
    private final String domainToAppNameReplace;

    public RegexDomainToAppName(final Pattern domainToAppNameRegex, final String domainToAppNameReplace) {
        this.domainToAppNameRegex = domainToAppNameRegex;
        this.domainToAppNameReplace = domainToAppNameReplace;
    }

    @Override
    public String apply(final String s) {
        final Matcher matcher = domainToAppNameRegex.matcher(s);
        if (matcher.find()) {
            return matcher.replaceAll(domainToAppNameReplace);
        }
        return null;
    }
}

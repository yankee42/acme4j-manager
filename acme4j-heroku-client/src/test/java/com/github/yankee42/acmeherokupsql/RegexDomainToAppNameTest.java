package com.github.yankee42.acmeherokupsql;

import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class RegexDomainToAppNameTest {
    private RegexDomainToAppName regexDomainToAppName =
        new RegexDomainToAppName(Pattern.compile("^([^.]+).tld$"), "my-app-$1");

    @Test
    public void returnsNullIfDomainDoesNotMatch() throws Exception {
        // execution
        final String actual = regexDomainToAppName.apply("does not match");

        // evaluation
        assertThat(actual, nullValue());
    }

    @Test
    public void returnsAppNameIfDomainDoesNotMatch() throws Exception {
        // execution
        final String actual = regexDomainToAppName.apply("foobar.tld");

        // evaluation
        assertThat(actual, equalTo("my-app-foobar"));
    }
}

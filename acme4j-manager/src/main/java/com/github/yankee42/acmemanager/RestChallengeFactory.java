package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * ChallengeFactory that uploads the token to the web server using an HTTP PUT request.
 */
public class RestChallengeFactory implements ChallengeFactory {

    private final Consumer<HttpURLConnection> requestProcessor;

    public RestChallengeFactory() {
        requestProcessor = x -> {};
    }

    /**
     *
     * @param requestProcessor the request processor can be used to augment the PUT request before it is send, e.g. to
     *                         add authentication credentials
     */
    public RestChallengeFactory(final Consumer<HttpURLConnection> requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @Override
    public Challenge createChallenge(final Authorization auth, final String domain) {
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
        if (challenge == null) {
            throw new Error("Found no " + Http01Challenge.TYPE + " challenge, don't know what to do...");
        }

        try {
            deployToken(domain, challenge);
        } catch (IOException e) {
            throw new Error(e);
        }

        return challenge;
    }

    private void deployToken(final String domain, final Http01Challenge challenge) throws IOException {

        final byte[] authBinary = challenge.getAuthorization().getBytes(StandardCharsets.UTF_8);
        final URL tokenUrl = new URL("http://" + domain + "/.well-known/acme-challenge/" + challenge.getToken());
        final HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
        connection.setFixedLengthStreamingMode(authBinary.length);
        connection.setDoOutput(true);
        requestProcessor.accept(connection);
        try {
            try (OutputStream out = connection.getOutputStream()) {
                out.write(authBinary);
            }
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Token upload failed: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } finally {
            connection.disconnect();
        }
    }
}

package com.github.yankee42.acmedeployheroku;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HerokoApi {
    private final String token;

    public HerokoApi(final String token) {
        this.token = token;
    }

    public void updateHerokuSslEndpoint(final String appIdOrName,
                                        final String sniEndpointIdOrName,
                                        final SniEndpointCreateUpdatePayload payload) throws IOException {

        doRequest("https://api.heroku.com/apps/" + appIdOrName + "/sni-endpoints/" + sniEndpointIdOrName, "PATCH", payload, null);
    }

    public void createHerokuSniEndpoint(final String appIdOrName, final SniEndpointCreateUpdatePayload payload) throws IOException {
        doRequest("https://api.heroku.com/apps/" + appIdOrName + "/sni-endpoints", "POST", payload, null);
    }

    public List<SniEndpointListPayload> listHerokuSniEndpoints(final String appIdOrName) throws IOException {
        return doRequest("https://api.heroku.com/apps/" + appIdOrName + "/sni-endpoints", "GET", null, new TypeReference<List<SniEndpointListPayload>>() {});
    }

    private<T> T doRequest(final String url, final String method, final Object payload, final TypeReference<T> responseType) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        if ("PATCH".equals(method)) {
            urlConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            urlConnection.setRequestMethod("POST");
        } else {
            urlConnection.setRequestMethod(method);
        }
        urlConnection.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");
        urlConnection.setRequestProperty("Authorization", "Bearer " + token);
        if (payload != null) {
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream outputStream = urlConnection.getOutputStream()) {
                outputStream.write(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsBytes(payload));
            }
        }
        if (urlConnection.getResponseCode() / 100 != 2) {
            throw new RuntimeException("Http request failed: " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
        }
        if (responseType != null) {
            try (InputStream in = urlConnection.getInputStream()) {
                return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(in, responseType);
            }
        }
        return null;
    }
}

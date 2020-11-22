package io.nosqlbench.engine.clients.grafana;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

public class GrafanaClientConfig {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @JsonProperty("baseuri")
    private URI baseUrl;

    @JsonProperty("timeoutms" )
    private int timeoutms;

    private final List<Authenticator> authenticators = new ArrayList<>();
    private final List<Supplier<Map<String, String>>> headerSources = new ArrayList<>();

    public GrafanaClientConfig() {
    }

    private GrafanaClientConfig(URI baseUrl, int timeoutms, List<Authenticator> authenticators,
                                List<Supplier<Map<String, String>>> headerSources) {
        this.baseUrl = baseUrl;
        this.timeoutms = timeoutms;
        this.authenticators.addAll(authenticators);
        this.headerSources.addAll(headerSources);
    }

    public GrafanaClientConfig copy() {
        return new GrafanaClientConfig(baseUrl, timeoutms, authenticators, headerSources);
    }

    public GrafanaClientConfig basicAuth(String username, String pw) {
        Objects.requireNonNull(username);
        String authPw = pw != null ? pw : "";

        Authenticator basicAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, authPw.toCharArray());
            }
        };

        addAuthenticator(basicAuth);
        addHeader("Authorization", encodeBasicAuth(username, authPw));
        return this;
    }

    public GrafanaClientConfig addAuthenticator(Authenticator authenticator) {
        authenticators.add(authenticator);
        return this;
    }

    public GrafanaClientConfig addHeader(String headername, String... headervals) {
        String headerVal = String.join(";", Arrays.asList(headervals));
        addHeaderSource(() -> Map.of(headername, headerVal));
        return this;
    }

    /**
     * Add a dynamic header source to be used for every new request.
     * Each source provides a map of new headers. If key or value of any
     * entry is null or empty, that entry is skipped. Otherwise, they are
     * computed and added to every request anew.
     *
     * @param headerSource A source of new headers
     * @return this GrafanaClientConfig, for method chaining
     */
    public GrafanaClientConfig addHeaderSource(Supplier<Map<String, String>> headerSource) {
        this.headerSources.add(headerSource);
        return this;
    }

    public LinkedHashMap<String, String> getHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        this.headerSources.forEach(hs -> {
            Map<String, String> entries = hs.get();
            entries.forEach((k, v) -> {
                if (k != null && v != null && !k.isEmpty() && !v.isEmpty()) {
                    headers.put(k, v);
                }
            });
        });
        return headers;
    }

    public HttpClient newClient() {
        HttpClient.Builder cb = HttpClient.newBuilder();
        if (timeoutms > 0) {
            cb.connectTimeout(Duration.ofMillis(timeoutms));
        }
        for (Authenticator authenticator : authenticators) {
            cb.authenticator(authenticator);
        }
        HttpClient client = cb.build();
        return client;
    }

    private URI makeUri(String pathAndQuery) {
        try {
            return new URI(getBaseUri().toString() + pathAndQuery);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpRequest.Builder newRequest(String path) {
        URI requestUri = makeUri(path);
        HttpRequest.Builder rqb = HttpRequest.newBuilder(requestUri);
        if (timeoutms > 0) {
            rqb.timeout(Duration.ofMillis(timeoutms));
        }
        getHeaders().forEach(rqb::setHeader);

        return rqb;
    }

    public GrafanaClientConfig setBaseUri(String baseuri) {
        try {
            URI uri = new URI(baseuri);
            String userinfo = uri.getRawUserInfo();
            if (userinfo != null) {
                String[] unpw = userinfo.split(":");
                basicAuth(unpw[0], unpw.length == 2 ? unpw[1] : "");
                uri = new URI(baseuri.replace(userinfo + "@", ""));
            }
            this.baseUrl = uri;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static GrafanaClientConfig fromJson(CharSequence json) {
        GrafanaClientConfig grafanaClientConfig = gson.fromJson(json.toString(), GrafanaClientConfig.class);
        return grafanaClientConfig;
    }

    public URI getBaseUri() {
        return baseUrl;
    }

    public HttpRequest newJsonPOST(String pathAndParams, Object rq) {
        HttpRequest.Builder rqb = newRequest(pathAndParams);
        String body = gson.toJson(rq);
        rqb = rqb.POST(HttpRequest.BodyPublishers.ofString(body));
        rqb = rqb.setHeader("Content-Type", "application/json");
        return rqb.build();
    }

    public int getTimeoutms() {
        return timeoutms;
    }

    public void setTimeoutms(int timeoutms) {
        this.timeoutms = timeoutms;
    }
}

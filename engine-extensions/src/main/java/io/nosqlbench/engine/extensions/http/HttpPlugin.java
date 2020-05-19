package io.nosqlbench.engine.extensions.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpPlugin {
    private HttpClient client = HttpClient.newHttpClient();

    public HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        URI uri = URI.create(url);
        HttpRequest request = builder
            .uri(uri)
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String> post(String url) throws IOException, InterruptedException {
        return post(url, null, null);
    }

    public HttpResponse<String> post(String url, String data, String contentType) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        URI uri = URI.create(url);

        HttpRequest request;
        if (data == null && contentType == null || contentType == null){
            request = builder
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } else if (data == null) {
            request = builder
                .uri(uri)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } else {
            request = builder
                .uri(uri)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        }

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        return response;
    }

}

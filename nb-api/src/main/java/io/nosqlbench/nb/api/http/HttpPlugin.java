/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.http;

import io.nosqlbench.nb.api.components.NBBaseComponent;
import io.nosqlbench.nb.api.components.NBComponent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpPlugin extends NBBaseComponent {
    private final HttpClient client = HttpClient.newHttpClient();

    public HttpPlugin(NBComponent parentComponent) {
        super(parentComponent);
    }

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
        if (contentType == null){
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

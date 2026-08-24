/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/** Small authenticated HTTP helper used by manifests and binary sources. */
public final class HttpTransport {
    private static final int MAX_ATTEMPTS = 3;
    private final OkHttpClient client;
    private final Optional<String> token;

    public HttpTransport(VectorDataSettings settings) {
        Duration timeout = settings.timeout();
        client = new OkHttpClient.Builder().callTimeout(timeout).connectTimeout(timeout).readTimeout(timeout).build();
        token = settings.bearerToken();
    }

    public byte[] get(URI uri) { return execute(uri, null, "GET"); }
    public byte[] range(URI uri, long from, long to) {
        if (from < 0 || to < from) throw new VectorDataException("Invalid HTTP range " + from + "-" + to);
        return execute(uri, "bytes=" + from + "-" + to, "GET");
    }
    public Probe probe(URI uri) {
        Request request = request(uri).head().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                long length = response.header("Content-Length", "-1").equals("-1") ? -1L : Long.parseLong(response.header("Content-Length"));
                return new Probe(length, "bytes".equalsIgnoreCase(response.header("Accept-Ranges")));
            }
        } catch (IOException ignored) { }
        Request rangeRequest = request(uri).header("Range", "bytes=0-0").get().build();
        try (Response response = client.newCall(rangeRequest).execute()) {
            if (!response.isSuccessful()) throw failure(uri, response);
            String contentRange = response.header("Content-Range");
            if (contentRange != null && contentRange.contains("/")) {
                return new Probe(Long.parseLong(contentRange.substring(contentRange.lastIndexOf('/') + 1)), true);
            }
            long length = response.body() == null ? -1L : response.body().contentLength();
            return new Probe(length, false);
        } catch (IOException e) { throw new VectorDataException("Cannot probe " + uri, e); }
    }
    private byte[] execute(URI uri, String range, String method) {
        Request.Builder builder = request(uri);
        if (range != null) builder.header("Range", range);
        if ("GET".equals(method)) builder.get();
        VectorDataException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try (Response response = client.newCall(builder.build()).execute()) {
                if (!response.isSuccessful()) {
                    HttpStatusException failure = failure(uri, response);
                    if (!transientStatus(response.code()) || attempt == MAX_ATTEMPTS) throw failure;
                    last = failure;
                } else {
                    ResponseBody body = response.body();
                    if (body == null) throw new VectorDataException("Empty HTTP response for " + uri);
                    return body.bytes();
                }
            } catch (IOException e) {
                last = new VectorDataException("HTTP request failed for " + uri, e);
                if (attempt == MAX_ATTEMPTS) throw last;
            }
            try { Thread.sleep(25L * attempt); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new VectorDataException("HTTP retry interrupted for " + uri, e); }
        }
        throw last == null ? new VectorDataException("HTTP request failed for " + uri) : last;
    }
    private Request.Builder request(URI uri) {
        Request.Builder request = new Request.Builder().url(uri.toString());
        token.ifPresent(value -> request.header("Authorization", "Bearer " + value));
        return request;
    }
    private static HttpStatusException failure(URI uri, Response response) {
        return new HttpStatusException("HTTP " + response.code() + " for " + uri, response.code());
    }
    private static boolean transientStatus(int status) { return status == 408 || status == 429 || status >= 500; }
    public record Probe(long contentLength, boolean ranges) { }
}

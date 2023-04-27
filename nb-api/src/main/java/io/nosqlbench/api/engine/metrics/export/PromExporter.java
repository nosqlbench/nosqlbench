/*
 * Copyright (c) 2023 nosqlbench
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
 *
 */

package io.nosqlbench.api.engine.metrics.export;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import org.apache.http.HttpException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;

import static java.util.Objects.requireNonNullElseGet;

public class PromExporter {

    private static final int DEFAULT_PORT = 8080;
    private PushGateway gateway;
    private CollectorRegistry collector;

    private MeterRegistry registry;

    public PromExporter() {
    }

    public void expose(PrometheusMeterRegistry registry) throws HttpException {

        try {
            this.registry = requireNonNullElseGet(registry, PromController::createRegistry);
            HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = registry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            new Thread(server::start).start();
        } catch (IOException e) {
            throw new HttpException("Failure while expos" +
                    "ing Prometheus endpoint", e);
        }
    }

    public MeterRegistry createRegistryWithPusher(URI uri) throws MalformedURLException {
        this.collector = CollectorRegistry.defaultRegistry;
        this.registry = PromController.createRegistryWithCollector();
        this.gateway = new PushGateway(uri.toURL());
        return this.registry;
    }

    public void push(String name) throws IOException {
        this.gateway.push(this.collector, name);
    }

}

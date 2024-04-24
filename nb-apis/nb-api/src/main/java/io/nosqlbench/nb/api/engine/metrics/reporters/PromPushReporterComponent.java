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
 */

package io.nosqlbench.nb.api.engine.metrics.reporters;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.system.NBEnvironment;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.PeriodicTaskComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public class PromPushReporterComponent extends PeriodicTaskComponent {
    private static final Logger logger = LogManager.getLogger(PromPushReporterComponent.class);
    private final Path keyfilePath;
    private HttpClient client;
    private final URI uri;
    private String bearerToken;

    public PromPushReporterComponent(NBComponent parent, String endpoint, long intervalMs, NBLabels nbLabels) {
        super(parent, nbLabels.and("_type", "prom-push"), intervalMs,  "REPORT-PROMPUSH",FirstReport.OnInterval, LastReport.OnInterrupt);
        String jobname = getLabels().valueOfOptional("jobname").orElse("default");
        String instance = getLabels().valueOfOptional("instance").orElse("default");
        if (jobname.equals("default") || instance.equals("default")) {
            logger.warn("It is highly recommended that you set a value for labels jobname and instance other than 'default'.");
        }

        if (endpoint.matches("victoria:[a-zA-Z0-9._-]+:[0-9]+")) {
            String[] parts = endpoint.split(":", 2);
            endpoint = "https://" + parts[1] + "/api/v1/import/prometheus/metrics/job/JOBNAME/instance/INSTANCE";
        }
        endpoint = endpoint.replace("JOBNAME", jobname).replace("INSTANCE", instance);
        if (!endpoint.contains(jobname)) {
            throw new BasicError("Mismatch between jobname in prompush URI and specified jobname label. You should use the short form for --report-prompush-to victoria:addr:port and set the jobname with --add-labels");
        }
        if (!endpoint.contains(instance)) {
            throw new BasicError("Mismatch between instance in prompush URI and specified instance label. You should use the short form for --report-prompush-to victoria:addr:port and set the instance with --add-labels");
        }
        this.uri = URI.create(endpoint);
        this.keyfilePath = NBEnvironment.INSTANCE
            .interpolateWithTimestamp("$NBSTATEDIR/prompush/prompush_apikey", System.currentTimeMillis())
            .map(Path::of)
            .orElseThrow(() -> new RuntimeException("Unable to create path for apikey file: $NBSTATEDIR/prompush/prompush_apikey"));
        if (Files.isRegularFile(keyfilePath)) {
            try {
                logger.info("Reading Bearer Token from {}", keyfilePath);
                this.bearerToken = Files.readString(keyfilePath).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void task() {
        final Clock nowclock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder(1024 * 1024); // 1M pre-allocated to reduce heap churn

        int total = 0;
        for (final Object metric : getParent().find().metrics()) {
            sb = PromExpositionFormat.format(nowclock, sb, metric);
            total++;
        }
        PromPushReporterComponent.logger.debug("formatted {} metrics in prom expo format", total);
        final String exposition = sb.toString();
        logger.trace(() -> "prom exposition format:\n" + exposition);

        final double backoffRatio = 1.5;
        final double maxBackoffSeconds = 10;
        double backOff = 1.0;

        final int maxRetries = 5;
        int remainingRetries = maxRetries;
        final List<Exception> errors = new ArrayList<>();
        boolean succeeded = false;

        while (0 < remainingRetries) {
            remainingRetries--;
            final HttpClient client = getCachedClient();
            final HttpRequest.Builder rb = HttpRequest.newBuilder().uri(uri);
            if (bearerToken != null) {
                rb.setHeader("Authorization", "Bearer " + bearerToken);
            }
            getComponentProp("prompush_cache")
                .map(cache -> Path.of(getComponentProp("logsdir").orElse("."))
                    .resolve("cache")).ifPresent(
                    prompush_cache_path -> {
                        try {
                            Files.writeString(
                                prompush_cache_path,
                                    exposition,
                                StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            final HttpRequest request = rb.POST(BodyPublishers.ofString(exposition)).build();
            final BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, handler);
                final int status = response.statusCode();
                if ((200 > status) || (300 <= status)) {
                    final String errmsg = "status " + response.statusCode() + " while posting metrics to '" + this.uri + '\'';
                    throw new RuntimeException(errmsg);
                }
                PromPushReporterComponent.logger.debug("posted {} metrics to prom push endpoint '{}'", total, this.uri);
                succeeded = true;
                break;
            } catch (final Exception e) {
                errors.add(e);
                try {
                    Thread.sleep((int) backOff * 1000L);
                } catch (final InterruptedException ignored) {
                }
                backOff = Math.min(maxBackoffSeconds, backOff * backoffRatio);
            }
        }
        if (!succeeded) {
            PromPushReporterComponent.logger.error("Failed to send push prom metrics after {} tries. Errors follow:", maxRetries);
            for (final Exception error : errors) PromPushReporterComponent.logger.error(error);
        }
    }

    private synchronized HttpClient getCachedClient() {
        if (null == client) this.client = this.getNewClient();
        return this.client;
    }

    private synchronized HttpClient getNewClient() {
        this.client = HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(60))
            .version(Version.HTTP_2)
            .build();
        return this.client;
    }
}

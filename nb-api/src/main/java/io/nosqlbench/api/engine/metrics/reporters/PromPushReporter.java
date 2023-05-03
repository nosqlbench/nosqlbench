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

package io.nosqlbench.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class PromPushReporter extends ScheduledReporter {
    private static final Logger logger = LogManager.getLogger(PromPushReporter.class);
    private HttpClient client;
    private final URI uri;

    public PromPushReporter(
        final String targetUriSpec,
        MetricRegistry registry,
        String name,
        MetricFilter filter,
        TimeUnit rateUnit,
        TimeUnit durationUnit
    ) {
        super(registry, name, filter, rateUnit, durationUnit);
        uri = URI.create(targetUriSpec);
    }

    @Override
    public synchronized void report(
        SortedMap<String, Gauge> gauges,
        SortedMap<String, Counter> counters,
        SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters,
        SortedMap<String, Timer> timers
    ) {
        final java.time.Clock nowclock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder(1024*1024); // 1M pre-allocated to reduce heap churn

        int total=0;
        for(final SortedMap smap : new SortedMap[]{gauges,counters,histograms,meters,timers})
            for (final Object metric : smap.values()) {
                sb = PromExpositionFormat.format(nowclock, sb, metric);
                total++;
            }
        PromPushReporter.logger.debug("formatted {} metrics in prom expo format", total);
        final String exposition = sb.toString();

        final double backoffRatio=1.5;
        final double maxBackoffSeconds=10;
        double backOff = 1.0;

        final int maxRetries = 5;
        int remainingRetries = maxRetries;
        final List<Exception> errors = new ArrayList<>();
        boolean succeeded=false;

        while (0 < remainingRetries) {
            remainingRetries--;
            final HttpClient client = getCachedClient();
            final HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(BodyPublishers.ofString(exposition)).build();
            final BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, handler);
                final int status = response.statusCode();
                if ((200 > status) || (300 <= status)) {
                    final String errmsg = "status " + response.statusCode() + " while posting metrics to '" + this.uri + '\'';
                    throw new RuntimeException(errmsg);
                }
                PromPushReporter.logger.debug("posted {} metrics to prom push endpoint '{}'", total, this.uri);
                succeeded=true;
                break;
            } catch (final Exception e) {
                errors.add(e);
                try {
                    Thread.sleep((int)backOff * 1000L);
                } catch (final InterruptedException ignored) {
                }
                backOff = Math.min(maxBackoffSeconds,backOff*backoffRatio);
            }
        }
        if (!succeeded) {
            PromPushReporter.logger.error("Failed to send push prom metrics after {} tries. Errors follow:", maxRetries);
            for (final Exception error : errors) PromPushReporter.logger.error(error);
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

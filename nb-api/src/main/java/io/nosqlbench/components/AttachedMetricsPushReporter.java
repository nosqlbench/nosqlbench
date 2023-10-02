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

package io.nosqlbench.components;

import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.engine.metrics.reporters.PromExpositionFormat;
import io.nosqlbench.api.engine.metrics.reporters.PromPushKeyFileReader;
import io.nosqlbench.api.labels.NBLabels;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AttachedMetricsPushReporter extends NBBaseComponent implements NBConfigurable, Runnable {

    private static final Logger logger = LogManager.getLogger(AttachedMetricsPushReporter.class);
    private final int intervalSeconds;
    private HttpClient client;
    private final URI uri;
    private String bearerToken;
    private boolean needsAuth;

    private Lock lock = new ReentrantLock(false);
    private Condition shutdownSignal = lock.newCondition();

    public AttachedMetricsPushReporter(
        final String targetUriSpec,
        NBComponent node,
        int seconds,
        NBLabels extraLabels
    ) {
        super(node, extraLabels);
        this.intervalSeconds = seconds;
        uri = URI.create(targetUriSpec);
        needsAuth = false;

        String config = "";
        ConfigLoader loader = new ConfigLoader();
        List<Map> configs = loader.load(config, Map.class);
        NBConfigModel cm = this.getConfigModel();
        if (configs != null) {
            logger.info("PromPushReporter process configuration: %s", config);
            for (Map cmap : configs) {
                NBConfiguration cfg = cm.apply(cmap);
                this.applyConfig(cfg);
            }
        } else {
            logger.info("PromPushReporter default configuration");
            HashMap<String, String> junk = new HashMap<>(Map.of());
            NBConfiguration cfg = cm.apply(junk);
            this.applyConfig(cfg);
        }

        Thread.ofVirtual().start(this);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .add(Param.defaultTo("apikeyfile", "$NBSTATEDIR/prompush/prompush_apikey")
                .setDescription("The file that contains the api key, supersedes apikey"))
            .add(Param.optional("apikey", String.class)
                .setDescription("The api key to use"))
            .asReadOnly();
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        Path keyfilePath = null;
        Optional<String> optionalApikeyfile = cfg.getEnvOptional("apikeyfile");
        Optional<String> optionalApikey = cfg.getOptional("apikey");
        bearerToken = null;
        if (optionalApikeyfile.isPresent()) {
            keyfilePath = optionalApikeyfile.map(Path::of).orElseThrow();
            if (Files.isRegularFile(keyfilePath)) {
                logger.info("Reading Bearer Token from %s", keyfilePath);
                PromPushKeyFileReader keyfile = new PromPushKeyFileReader(keyfilePath);
                bearerToken = keyfile.get();
            }
        } else if (optionalApikey.isPresent()) {
            bearerToken = optionalApikey.get();
        }
        needsAuth = (null != bearerToken);
        bearerToken = "Bearer " + bearerToken;
    }

    public synchronized void report() {
        final Clock nowclock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder(1024 * 1024); // 1M pre-allocated to reduce heap churn
        List<NBMetric> metrics = new ArrayList<>();
        Iterator<NBComponent> allMetrics = NBComponentTraversal.traverseBreadth(getParent());
        allMetrics.forEachRemaining(m -> metrics.addAll(m.findMetrics("")));

        int total = 0;
        for (NBMetric metric : metrics) {
            sb = PromExpositionFormat.format(nowclock, sb, metric);
            total++;
        }
        AttachedMetricsPushReporter.logger.debug("formatted {} metrics in prom expo format", total);
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
            if (needsAuth) {
                rb.setHeader("Authorization", bearerToken);
            }
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
                AttachedMetricsPushReporter.logger.debug("posted {} metrics to prom push endpoint '{}'", total, this.uri);
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
            AttachedMetricsPushReporter.logger.error("Failed to send push prom metrics after {} tries. Errors follow:", maxRetries);
            for (final Exception error : errors) AttachedMetricsPushReporter.logger.error(error);
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

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long reportAt = now + intervalSeconds * 1000L;
        long waitfor = reportAt - now;

        loop:
        while (true) {

            while (waitfor > 0) {
                try {
                    if (shutdownSignal.await(waitfor, TimeUnit.MILLISECONDS)) {
                        logger.debug("shutting down " + this);
                        break loop;
                    }
                    now = System.currentTimeMillis();
                    waitfor = now - reportAt;
                } catch (InterruptedException ignored) {
                }
                logger.info("reporting metrics via push");
                try {
                    report();
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    reportAt = now;
                    now = System.currentTimeMillis();
                    waitfor = now - reportAt;
                }
            }
        }
        logger.info("reporter thread shutting down");
    }

    @Override
    public void beforeDetach() {
        this.shutdown();
    }

    private void shutdown() {
        logger.debug("shutting down " + this);

        lock.lock();
        shutdownSignal.signal();
        lock.unlock();
    }
}

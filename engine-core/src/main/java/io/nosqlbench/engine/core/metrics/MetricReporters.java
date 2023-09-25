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

package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.nosqlbench.api.engine.metrics.MetricsRegistry;
import io.nosqlbench.api.engine.metrics.NBMetricsRegistry;
import io.nosqlbench.api.engine.metrics.reporters.PromPushReporter;
import io.nosqlbench.api.Shutdownable;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.lifecycle.process.ShutdownManager;
import io.nosqlbench.api.engine.metrics.reporters.Log4JMetricsReporter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MetricReporters implements Shutdownable {
    private static final Logger logger = LogManager.getLogger(MetricReporters.class);
    private static final MetricReporters instance = new MetricReporters();

    private final List<PrefixedRegistry> metricRegistries = new ArrayList<>();
    private final List<ScheduledReporter> scheduledReporters = new ArrayList<>();

    private MetricReporters() {
        ShutdownManager.register(this);
    }

    public static MetricReporters getInstance() {
        return instance;
    }

    public MetricReporters addRegistry(String registryPrefix, MetricsRegistry metricsRegistry) {
        this.metricRegistries.add(new PrefixedRegistry(registryPrefix, metricsRegistry));
        return this;
    }

    public MetricReporters addGraphite(String dest, String prefix) {
        logger.debug(() -> "Adding graphite reporter to " + dest + " with prefix " + prefix);
        if (0 <= dest.indexOf(':')) {
            String[] split = dest.split(":");
            addGraphite(split[0],Integer.valueOf(split[1]),prefix);
        } else {
            addGraphite(dest, 2003, prefix);
        }
        return this;
    }

    public void addCSVReporter(String directoryName, String prefix) {
        logger.debug(() -> "Adding CSV reporter to " + directoryName + " with prefix " + prefix);

        if (metricRegistries.isEmpty()) {
            throw new RuntimeException("There are no metric registries.");
        }

        File csvDirectory = new File(directoryName);
        if (!csvDirectory.exists()) {
            if (!csvDirectory.mkdirs()) {
                throw new RuntimeException("Error creating CSV reporting directory:" + csvDirectory.getAbsolutePath());
            }
        }

        for (PrefixedRegistry prefixedRegistry : metricRegistries) {
            if (prefixedRegistry.metricRegistry instanceof NBMetricsRegistry) {
                CsvReporter csvReporter = CsvReporter.forRegistry(
                    (NBMetricsRegistry)prefixedRegistry.metricRegistry)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .formatFor(Locale.US)
                    .build(csvDirectory);

                scheduledReporters.add(csvReporter);
            } else {
                throw new RuntimeException("MetricsRegistry type " +
                    prefixedRegistry.metricRegistry.getClass().getCanonicalName() + " is not supported.");
            }
        }
    }

    public MetricReporters addGraphite(String host, int graphitePort, String globalPrefix) {

        logger.debug(() -> "Adding graphite reporter to " + host + " with port " + graphitePort + " and prefix " + globalPrefix);

        if (metricRegistries.isEmpty()) {
            throw new RuntimeException("There are no metric registries.");
        }

        for (PrefixedRegistry prefixedRegistry : metricRegistries) {

            Graphite graphite = new Graphite(new InetSocketAddress(host, graphitePort));
            String _prefix = null != prefixedRegistry.prefix ? !prefixedRegistry.prefix.isEmpty() ? globalPrefix + '.' + prefixedRegistry.prefix : globalPrefix : globalPrefix;
            if (prefixedRegistry.metricRegistry instanceof NBMetricsRegistry) {
                GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry((NBMetricsRegistry) prefixedRegistry.metricRegistry)
                    .prefixedWith(_prefix)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .build(graphite);

                scheduledReporters.add(graphiteReporter);
            } else {
                throw new RuntimeException("MetricsRegistry type " +
                    prefixedRegistry.metricRegistry.getClass().getCanonicalName() + " is not supported.");
            }
        }
        return this;
    }

    public MetricReporters addPromPush(final String reportPromPushTo, final String prefix, final String config) {

        logger.debug(() -> "Adding prompush reporter to " + reportPromPushTo + " with prefix label to " + prefix);

        if (metricRegistries.isEmpty()) {
            throw new RuntimeException("There are no metric registries.");
        }

        for (PrefixedRegistry prefixedRegistry : metricRegistries) {
            if (prefixedRegistry.metricRegistry instanceof NBMetricsRegistry) {
                final PromPushReporter promPushReporter =
                    new PromPushReporter(
                        reportPromPushTo,
                        (NBMetricsRegistry) prefixedRegistry.metricRegistry,
                        "prompush",
                        MetricFilter.ALL,
                        TimeUnit.SECONDS,
                        TimeUnit.NANOSECONDS,
                        config
                    );
                scheduledReporters.add(promPushReporter);
            } else {
                throw new RuntimeException("MetricsRegistry type " +
                    prefixedRegistry.metricRegistry.getClass().getCanonicalName() + " is not supported.");
            }
        }
        return this;
    }


    public MetricReporters addLogger() {
        logger.debug("Adding log4j reporter for metrics");

        if (metricRegistries.isEmpty()) {
            throw new RuntimeException("There are no metric registries.");
        }

        for (PrefixedRegistry prefixedRegistry : metricRegistries) {

            Log4JMetricsReporter reporter4j = Log4JMetricsReporter.forRegistry(prefixedRegistry.metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .outputTo(logger)
                    .build();

            scheduledReporters.add(reporter4j);
        }
        return this;
    }

    public MetricReporters start(int consoleIntervalSeconds, int remoteIntervalSeconds) {
        for (ScheduledReporter scheduledReporter : scheduledReporters) {
            logger.info(() -> "starting reporter: " + scheduledReporter.getClass().getSimpleName());
            if (scheduledReporter instanceof ConsoleReporter) {
                scheduledReporter.start(consoleIntervalSeconds, TimeUnit.SECONDS);
            } else {
                scheduledReporter.start(remoteIntervalSeconds, TimeUnit.SECONDS);
            }
        }
        return this;
    }

    public MetricReporters stop() {
        for (ScheduledReporter scheduledReporter : scheduledReporters) {
            logger.info(() -> "stopping reporter: " + scheduledReporter);
            scheduledReporter.stop();
        }
        return this;
    }


    public MetricReporters report() {
        for (ScheduledReporter scheduledReporter : scheduledReporters) {
            logger.info(() -> "flushing reporter data: " + scheduledReporter);
            scheduledReporter.report();
        }
        return this;
    }

    @Override
    public void shutdown() {
        for (ScheduledReporter reporter : scheduledReporters) {
            reporter.report();
            reporter.stop();
        }
    }


    private class PrefixedRegistry {
        public String prefix;
        public MetricsRegistry metricRegistry;

        public PrefixedRegistry(String prefix, MetricsRegistry metricRegistry) {
            this.prefix = prefix;
            this.metricRegistry = metricRegistry;
        }
    }
}

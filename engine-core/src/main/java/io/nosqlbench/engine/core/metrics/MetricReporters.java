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
import io.nosqlbench.engine.api.activityapi.core.Shutdownable;
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
        return MetricReporters.instance;
    }

    public MetricReporters addRegistry(final String registryPrefix, final MetricRegistry metricsRegistry) {
        metricRegistries.add(new PrefixedRegistry(registryPrefix, metricsRegistry));
        return this;
    }

    public MetricReporters addGraphite(final String dest, final String prefix) {
        MetricReporters.logger.debug(() -> "Adding graphite reporter to " + dest + " with prefix " + prefix);
        if (0 <= dest.indexOf(":")) {
            final String[] split = dest.split(":");
            this.addGraphite(split[0],Integer.valueOf(split[1]),prefix);
        } else this.addGraphite(dest, 2003, prefix);
        return this;
    }

    public void addCSVReporter(final String directoryName, final String prefix) {
        MetricReporters.logger.debug(() -> "Adding CSV reporter to " + directoryName + " with prefix " + prefix);

        if (this.metricRegistries.isEmpty()) throw new RuntimeException("There are no metric registries.");

        final File csvDirectory = new File(directoryName);
        if (!csvDirectory.exists()) if (!csvDirectory.mkdirs())
            throw new RuntimeException("Error creating CSV reporting directory:" + csvDirectory.getAbsolutePath());

        for (final PrefixedRegistry prefixedRegistry : this.metricRegistries) {
            final CsvReporter csvReporter = CsvReporter.forRegistry(prefixedRegistry.metricRegistry)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .formatFor(Locale.US)
                    .build(csvDirectory);

            this.scheduledReporters.add(csvReporter);
        }
    }

    public MetricReporters addGraphite(final String host, final int graphitePort, final String globalPrefix) {

        MetricReporters.logger.debug(() -> "Adding graphite reporter to " + host + " with port " + graphitePort + " and prefix " + globalPrefix);

        if (this.metricRegistries.isEmpty()) throw new RuntimeException("There are no metric registries.");

        for (final PrefixedRegistry prefixedRegistry : this.metricRegistries) {

            final Graphite graphite = new Graphite(new InetSocketAddress(host, graphitePort));
            final String _prefix = (null != prefixedRegistry.prefix) ? (!prefixedRegistry.prefix.isEmpty() ? (globalPrefix + '.' + prefixedRegistry.prefix) : globalPrefix) : globalPrefix;
            final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(prefixedRegistry.metricRegistry)
                    .prefixedWith(_prefix)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .build(graphite);

            this.scheduledReporters.add(graphiteReporter);
        }
        return this;
    }

    public MetricReporters addLogger() {
        MetricReporters.logger.debug("Adding log4j reporter for metrics");

        if (this.metricRegistries.isEmpty()) throw new RuntimeException("There are no metric registries.");

        for (final PrefixedRegistry prefixedRegistry : this.metricRegistries) {

            final Log4JMetricsReporter reporter4j = Log4JMetricsReporter.forRegistry(prefixedRegistry.metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.NANOSECONDS)
                    .filter(ActivityMetrics.METRIC_FILTER)
                    .outputTo(MetricReporters.logger)
                    .build();

            this.scheduledReporters.add(reporter4j);
        }
        return this;
    }

    public MetricReporters start(final int consoleIntervalSeconds, final int remoteIntervalSeconds) {
        for (final ScheduledReporter scheduledReporter : this.scheduledReporters) {
            MetricReporters.logger.info(() -> "starting reporter: " + scheduledReporter.getClass().getSimpleName());
            if (scheduledReporter instanceof ConsoleReporter)
                scheduledReporter.start(consoleIntervalSeconds, TimeUnit.SECONDS);
            else scheduledReporter.start(remoteIntervalSeconds, TimeUnit.SECONDS);
        }
        return this;
    }

    public MetricReporters stop() {
        for (final ScheduledReporter scheduledReporter : this.scheduledReporters) {
            MetricReporters.logger.info(() -> "stopping reporter: " + scheduledReporter);
            scheduledReporter.stop();
        }
        return this;
    }


    public MetricReporters report() {
        for (final ScheduledReporter scheduledReporter : this.scheduledReporters) {
            MetricReporters.logger.info(() -> "flushing reporter data: " + scheduledReporter);
            scheduledReporter.report();
        }
        return this;
    }

    @Override
    public void shutdown() {
        for (final ScheduledReporter reporter : this.scheduledReporters) {
            reporter.report();
            reporter.stop();
        }
    }

    private class PrefixedRegistry {
        public String prefix;
        public MetricRegistry metricRegistry;

        public PrefixedRegistry(final String prefix, final MetricRegistry metricRegistry) {
            this.prefix = prefix;
            this.metricRegistry = metricRegistry;
        }
    }
}

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

package io.nosqlbench.engine.extensions.csvmetrics;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.engine.metrics.MetricsRegistry;
import io.nosqlbench.api.engine.metrics.NBMetricsRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CSVMetrics {

    private final MetricsRegistry registry;
    private final File reportTo;
    CsvReporter reporter;

    /**
     * Create a CSV reporter that is not automatically logging.
     * @param directory a CSV logging filename
     * @param logger an extension logger, to be used for logging extension-specific events
     * @param registry a MetricRegistry to report
     */
    public CSVMetrics(String directory, Logger logger, MetricsRegistry registry) {
        File reportTo = new File(directory);
        if (!reportTo.exists()) {
            if (!reportTo.mkdirs()) {
                throw new RuntimeException("Unable to make directory: " + reportTo);
            }
        }
        this.reportTo = reportTo;
        this.registry = registry;
    }

    private void initReporter() {
        if (reporter!=null) {
            return;
        }
        if (registry instanceof NBMetricsRegistry) {
            reporter = CsvReporter.forRegistry((NBMetricsRegistry) registry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(filter)
                .build(reportTo);
        } else {
            throw new RuntimeException("MetricsRegistry type " + registry.getClass().getCanonicalName() + " is not supported.");
        }

    }

    /**
     * Create an autologging CSV Reporter with the specified period and time unit.
     * @param csvFile a CSV logging filename
     * @param logger an extension logger, to be used for logging extension-specific events
     * @param registry a MetricRegistry to report
     * @param period a period between reporting intervals
     * @param timeUnit the actual timeunit for the period
     */
    public CSVMetrics(String csvFile, Logger logger, MetricsRegistry registry, long period, TimeUnit timeUnit) {
        this(csvFile, logger, registry);
        reporter.start(period, timeUnit);
    }

    public CSVMetrics start(long period, String timeUnitName) {
        initReporter();
        TimeUnit timeUnit = TimeUnit.valueOf(timeUnitName);
        reporter.start(period, timeUnit);
        return this;
    }

    public CSVMetrics add(Metric metric) {
        Objects.requireNonNull(metric);
        filter.add(metric);
        return this;
    }

    public CSVMetrics addPattern(String regex) {
        filter.addPattern(regex);
        return this;
    }

    private final MetricInstanceFilter filter = new MetricInstanceFilter();

    public CSVMetrics stop() {
        reporter.stop();
        return this;
    }

    public CSVMetrics report() {
        initReporter();
        reporter.report();
        return this;
    }
}

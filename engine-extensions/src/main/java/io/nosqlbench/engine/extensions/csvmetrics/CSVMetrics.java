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

import com.codahale.metrics.Metric;
import io.nosqlbench.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBBuilders;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CSVMetrics extends NBBaseComponent {

    private NBBaseComponent baseComponent;
    private final NBBuilders builders;
    private final File reportTo;
    private CsvReporter reporter;
    private int interval = 1;
    private final Logger extensionLogger;

    /**
     * Create a CSV reporter that is not automatically logging.
     * @param baseComponent a NBBaseComponent instance to report
     * @param directory a CSV logging filename
     * @param logger an extension logger, to be used for logging extension-specific events
     */
    public CSVMetrics(NBBaseComponent baseComponent, String directory, Logger logger) {
        super(baseComponent);
        this.baseComponent = baseComponent;
        extensionLogger = logger;
        builders = new NBBuilders(baseComponent);
        File reportTo = new File(directory);
        if (!reportTo.exists()) {
            if (!reportTo.mkdirs()) {
                throw new RuntimeException("Unable to make directory: " + reportTo);
            }
        }
        this.reportTo = reportTo;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private void initReporter() {
        if (reporter!=null) {
            return;
        }
        reporter = builders.csvReporter(reportTo, interval);
    }

    /**
     * Create an autologging CSV Reporter with the specified period
     * @param csvFile a CSV logging filename
     * @param logger an extension logger, to be used for logging extension-specific events
     * @param baseComponent a NBBaseComponent instance to report
     * @param period a period between reporting intervals
     */
    public CSVMetrics(String csvFile, Logger logger, NBBaseComponent baseComponent, int period) {
        this(baseComponent, csvFile, logger);
        this.start(period);
    }

    public CSVMetrics start(int period) {
        setInterval(period);
        initReporter();
        reporter.task();
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
        reporter.teardown();
        return this;
    }

    public CSVMetrics report() {
        initReporter();
        reporter.report();
        return this;
    }
}

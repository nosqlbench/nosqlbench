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

import io.nosqlbench.api.csvoutput.CsvOutputPluginWriter;
import com.codahale.metrics.Meter;
import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.engine.metrics.DoubleSummaryGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.api.engine.metrics.instruments.*;
import io.nosqlbench.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.api.engine.metrics.reporters.PromPushReporterComponent;
import io.nosqlbench.api.histo.HdrHistoLog;
import io.nosqlbench.api.histo.HistoStats;
import io.nosqlbench.api.http.HttpPlugin;
import io.nosqlbench.api.optimizers.BobyqaOptimizerInstance;
import io.nosqlbench.api.files.FileAccess;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.shutdown.NBShutdownHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Supplier;

public class NBBuilders {

    // TODO: add mandatory sanitize() around all label names and label "name" values
    private final Logger logger = LogManager.getLogger(NBBuilders.class);
    private final NBBaseComponent base;

    public NBBuilders(NBBaseComponent base) {
        this.base = base;
    }

    public NBMetricTimer timer(String metricFamilyName) {
        return timer(metricFamilyName,4);
    }
    public NBMetricTimer timer(String metricFamilyName, int hdrdigits) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricTimer timer = new NBMetricTimer(labels, new DeltaHdrHistogramReservoir(labels, hdrdigits));
        base.addComponentMetric(timer);
        return timer;
    }

    public Meter meter(String metricFamilyName) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricMeter meter = new NBMetricMeter(labels);
        base.addComponentMetric(meter);
        return meter;
    }


    public NBMetricCounter counter(String metricFamilyName) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricCounter counter = new NBMetricCounter(labels);
        base.addComponentMetric(counter);
        return counter;
    }


    public NBFunctionGauge gauge(String metricFamilyName, Supplier<Double> valueSource) {
        NBFunctionGauge gauge = new NBFunctionGauge(base, valueSource, metricFamilyName);
        base.addComponentMetric(gauge);
        return gauge;
    }


    public DoubleSummaryGauge summaryGauge(String name, String... statspecs) {
        List<DoubleSummaryGauge.Stat> stats = Arrays.stream(statspecs).map(DoubleSummaryGauge.Stat::valueOf).toList();
        DoubleSummaryStatistics reservoir = new DoubleSummaryStatistics();
        DoubleSummaryGauge anyGauge = null;
        for (DoubleSummaryGauge.Stat stat : stats) {
            anyGauge = new DoubleSummaryGauge(base.getLabels().and(NBLabels.forKV("name",name,"stat", stat)), stat, reservoir);
            base.addComponentMetric(anyGauge);
        }
        return anyGauge;
    }

    public NBMetricHistogram histogram(String metricFamilyName) {
        return histogram(metricFamilyName,4);
    }
    public NBMetricHistogram histogram(String metricFamilyName, int hdrdigits) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricHistogram histogram = new NBMetricHistogram(labels, new DeltaHdrHistogramReservoir(labels, hdrdigits));
        base.addComponentMetric(histogram);
        return histogram;
    }

    public AttachedMetricsSummaryReporter summaryReporter(int seconds, String... labelspecs) {
        logger.debug("attaching summary reporter to " + base.description());
        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
        AttachedMetricsSummaryReporter reporter = new AttachedMetricsSummaryReporter(base, extraLabels, seconds);
        return reporter;
    }
//    public AttachedMetricCsvReporter csvReporter(int seconds, String dirpath, String... labelspecs) {
//        logger.debug("attaching summary reporter to " + base.description());
//        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
//        AttachedMetricCsvReporter reporter = new AttachedMetricCsvReporter(base, extraLabels, Path.of(dirpath), seconds);
//        return reporter;
//    }
    public PromPushReporterComponent pushReporter(String targetUri, int seconds, String config, String... labelspecs) {
        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
        PromPushReporterComponent reporter = new PromPushReporterComponent(targetUri, config, seconds, base,extraLabels);
        return reporter;
    }

//    public ExamplePlugin getExamplePlugin(final NBComponent component) {
//        return new ExamplePlugin(component);
//    }

    public BobyqaOptimizerInstance bobyqaOptimizer() {
        return new BobyqaOptimizerInstance(base);
    }

    public FileAccess fileAccess(String filename) {
        return new FileAccess();
    }

    public HdrHistoLog hdrHistoLog(NBComponent component) {
        return new HdrHistoLog(component);
    }

    public HistoStats histoStats(NBComponent component) {
        return new HistoStats(component);
    }

    public HttpPlugin httpPlugin(NBComponent component) {
        return new HttpPlugin(component);
    }

    public NBShutdownHook shutdownHook(NBComponent component) {
        return new NBShutdownHook(component);
    }

    public static class CsvOutputWriterBuilder {

        private final NBComponent component;
        private final String filename;
        private String[] headers;

        public CsvOutputWriterBuilder(NBComponent component, String filename) {
            this.component = component;
            this.filename = filename;
        }
        public CsvOutputWriterBuilder headers(String... headers) {
            this.headers = headers;
            return this;
        }
        public CsvOutputPluginWriter build() {
            return new CsvOutputPluginWriter(component, filename, headers);
        }
    }

    public static class CsvReporterBuilder {
        private final NBComponent component;
        private Path reportTo = Path.of("metrics.csv");
        private int interval = 1;
        private MetricInstanceFilter filter = new MetricInstanceFilter();
        private NBLabels labels = null;

        public CsvReporterBuilder(NBComponent component) {
            this.component = component;
        }
        public CsvReporterBuilder labels(NBLabels labels) {
            this.labels = labels;
            return this;
        }
        public CsvReporterBuilder path(Path reportTo) {
            this.reportTo = reportTo;
            return this;
        }
        public CsvReporterBuilder path(String reportTo) {
            this.reportTo = Path.of(reportTo);
            return this;
        }
        public CsvReporterBuilder interval(int interval) {
            this.interval = interval;
            return this;
        }
        public CsvReporterBuilder filter(MetricInstanceFilter filter) {
            this.filter = filter;
            return this;
        }
        public CsvReporter build() {
            return new CsvReporter(component, reportTo, interval, filter);
        }
    }

}

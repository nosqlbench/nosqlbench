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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.csvoutput.CsvOutputPluginWriter;
import com.codahale.metrics.Meter;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.DoubleSummaryGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;
import io.nosqlbench.nb.api.engine.metrics.reporters.*;
import io.nosqlbench.nb.api.histo.HdrHistoLog;
import io.nosqlbench.nb.api.histo.HistoStats;
import io.nosqlbench.nb.api.http.HttpPlugin;
import io.nosqlbench.nb.api.labels.MapLabels;
import io.nosqlbench.nb.api.optimizers.BobyqaOptimizerInstance;
import io.nosqlbench.nb.api.files.FileAccess;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.shutdown.NBShutdownHook;
import io.nosqlbench.nb.api.loaders.BundledExtensionsLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import org.apache.logging.log4j.Marker;

import java.io.PrintStream;
import java.util.*;

import java.nio.file.Path;
import java.util.function.Supplier;

public class NBCreators {

    // TODO: add mandatory sanitize() around all label names and label "name" values
    private final Logger logger = LogManager.getLogger(NBCreators.class);
    private final NBBaseComponent base;

    public NBCreators(NBBaseComponent base) {
        this.base = base;
    }

    public NBMetricTimer timer(String metricFamilyName) {
        return timer(metricFamilyName,3);
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

    public NBVariableGauge variableGauge(String metricFamilyName, double initialValue, String... additionalLabels) {
        NBVariableGauge gauge = new NBVariableGauge(base, metricFamilyName, initialValue, additionalLabels);
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

//    public AttachedMetricsSummaryReporter summaryReporter(long millis, String... labelspecs) {
//        logger.debug("attaching summary reporter to " + base.description());
//        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
//        AttachedMetricsSummaryReporter reporter = new AttachedMetricsSummaryReporter(base, extraLabels, millis);
//        return reporter;
//    }
//    public AttachedMetricCsvReporter csvReporter(int seconds, String dirpath, String... labelspecs) {
//        logger.debug("attaching summary reporter to " + base.description());
//        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
//        AttachedMetricCsvReporter reporter = new AttachedMetricCsvReporter(base, extraLabels, Path.of(dirpath), seconds);
//        return reporter;
//    }
    public PromPushReporterComponent pushReporter(String endpoint, long millis, NBLabels extraLabels) {
        PromPushReporterComponent reporter = new PromPushReporterComponent(this.base, endpoint, millis, extraLabels);
        return reporter;
    }

    public BobyqaOptimizerInstance bobyqaOptimizer() {
        return new BobyqaOptimizerInstance(base);
    }

    public FileAccess fileAccess() {
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

    public static class Log4jReporterBuilder {
        private final NBComponent component;
        private Logger logger = LogManager.getLogger(Log4JMetricsReporter.class);
        private Log4JMetricsReporter.LoggingLevel loggingLevel = Log4JMetricsReporter.LoggingLevel.INFO;
        private Marker marker;
        private MetricFilter filter= new MetricInstanceFilter();
        private boolean oneLastTime = false;
        private NBLabels labels;
        private long millis = 1000;

        public Log4jReporterBuilder(NBComponent component) {
            this.component = component;
        }
        public Log4jReporterBuilder oneLastTime(final boolean oneLastTime) {
            this.oneLastTime = oneLastTime;
            return this;
        }
        public Log4jReporterBuilder interval(final int interval) {
            this.millis = interval;
            return this;
        }
        public Log4jReporterBuilder outputTo(final Logger logger) {
            this.logger = logger;
            return this;
        }
        public Log4jReporterBuilder markWith(final Marker marker) {
            this.marker = marker;
            return this;
        }
        public Log4jReporterBuilder labels(final NBLabels labels) {
            this.labels = labels;
            return this;
        }
        public Log4jReporterBuilder filter(final MetricFilter filter) {
            this.filter = filter;
            return this;
        }
        public Log4jReporterBuilder withLoggingLevel(final Log4JMetricsReporter.LoggingLevel loggingLevel) {
            this.loggingLevel = loggingLevel;
            return this;
        }
        public Log4JMetricsReporter build() {
            final LoggerProxy loggerProxy = switch (this.loggingLevel) {
                case TRACE -> new TraceLoggerProxy(this.logger);
                case INFO -> new InfoLoggerProxy(this.logger);
                case WARN -> new WarnLoggerProxy(this.logger);
                case ERROR -> new ErrorLoggerProxy(this.logger);
                default -> new DebugLoggerProxy(this.logger);
            };
            return new Log4JMetricsReporter(this.component, loggerProxy, this.marker, this.filter, this.labels, this.millis, this.oneLastTime);
        }
    }
    /* private class to allow logger configuration */
    public abstract static class LoggerProxy {
        protected final Logger logger;

        protected LoggerProxy(final Logger logger) {
            this.logger = logger;
        }

        public abstract void log(Marker marker, String format, Object... arguments);

        public abstract boolean isEnabled(Marker marker);
    }

    /* private class to allow logger configuration */
    private static class DebugLoggerProxy extends LoggerProxy {
        public DebugLoggerProxy(final Logger logger) {
            super(logger);
        }

        @Override
        public void log(final Marker marker, final String format, final Object... arguments) {
            this.logger.debug(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(final Marker marker) {
            return this.logger.isDebugEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class TraceLoggerProxy extends LoggerProxy {
        public TraceLoggerProxy(final Logger logger) {
            super(logger);
        }

        @Override
        public void log(final Marker marker, final String format, final Object... arguments) {
            this.logger.trace(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(final Marker marker) {
            return this.logger.isTraceEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class InfoLoggerProxy extends LoggerProxy {
        public InfoLoggerProxy(final Logger logger) {
            super(logger);
        }

        @Override
        public void log(final Marker marker, final String format, final Object... arguments) {
            this.logger.info(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(final Marker marker) {
            return this.logger.isInfoEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class WarnLoggerProxy extends LoggerProxy {
        public WarnLoggerProxy(final Logger logger) {
            super(logger);
        }

        @Override
        public void log(final Marker marker, final String format, final Object... arguments) {
            this.logger.warn(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(final Marker marker) {
            return this.logger.isWarnEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class ErrorLoggerProxy extends LoggerProxy {
        public ErrorLoggerProxy(final Logger logger) {
            super(logger);
        }

        @Override
        public void log(final Marker marker, final String format, final Object... arguments) {
            this.logger.error(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(final Marker marker) {
            return this.logger.isErrorEnabled(marker);
        }

    }

    public static class ConsoleReporterBuilder {
        private final NBComponent component;
        private final PrintStream output;
        private NBLabels labels = new MapLabels(Map.of());
        private long interval = 1000;
        private boolean oneLastTime = false;
        private Set<MetricAttribute> disabledMetricAttributes = Set.of();

        public ConsoleReporterBuilder(NBComponent component, PrintStream output) {
            this.component = component;
            this.output = output;
        }
        public ConsoleReporterBuilder labels(NBLabels labels) {
            this.labels = labels;
            return this;
        }
        public ConsoleReporterBuilder interval(int interval) {
            this.interval = interval;
            return this;
        }
        public ConsoleReporterBuilder oneLastTime(boolean oneLastTime) {
            this.oneLastTime = oneLastTime;
            return this;
        }
        public ConsoleReporterBuilder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }
        public ConsoleReporter build() {
            return new ConsoleReporter(component, labels, interval, oneLastTime, output, disabledMetricAttributes);
        }
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
            return new CsvReporter(component, reportTo, interval, filter, labels);
        }
    }

    public <T> T requiredExtension(String name, Class<T> type) {
        return new BundledExtensionsLoader(base)
            .load(name, type)
            .orElseThrow(
                () -> new RuntimeException("unable to load extension with name '" + name + "' and type '" + type.getSimpleName() + "'")
            );
    }
}

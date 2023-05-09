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

package io.nosqlbench.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is a Log4J targeted metrics logging reporter, derived from
 * {@link Slf4jReporter}. This implementation
 * was built to allow for consolidating internal logging dependencies
 * to log4j only.
 */
public class Log4JMetricsReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for .
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a
     */
    public static Builder forRegistry(final MetricRegistry registry) {
        return new Builder(registry);
    }

    public enum LoggingLevel { TRACE, DEBUG, INFO, WARN, ERROR }

    /**
     * A builder for {@link Log4JMetricsReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private LoggingLevel loggingLevel;
        private Marker marker;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;

        private Builder(final MetricRegistry registry) {
            this.registry = registry;
            logger = LogManager.getLogger("metrics");
            marker = null;
            prefix = "";
            rateUnit = TimeUnit.SECONDS;
            durationUnit = TimeUnit.MILLISECONDS;
            filter = MetricFilter.ALL;
            loggingLevel = LoggingLevel.INFO;
            executor = null;
            shutdownExecutorOnStop = true;
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(final boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(final ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Log metrics to the given logger.
         *
         * @param logger an SLF4J {@link Logger}
         * @return {@code this}
         */
        public Builder outputTo(final Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker an SLF4J {@link Marker}
         * @return {@code this}
         */
        public Builder markWith(final Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(final TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(final TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(final MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Use Logging Level when reporting.
         *
         * @param loggingLevel a (@link Slf4jReporter.LoggingLevel}
         * @return {@code this}
         */
        public Builder withLoggingLevel(final LoggingLevel loggingLevel) {
            this.loggingLevel = loggingLevel;
            return this;
        }

        /**
         * Builds a {@link Log4JMetricsReporter} with the given properties.
         *
         * @return a {@link Log4JMetricsReporter}
         */
        public Log4JMetricsReporter build() {
            final LoggerProxy loggerProxy;
            switch (this.loggingLevel) {
                case TRACE:
                    loggerProxy = new TraceLoggerProxy(this.logger);
                    break;
                case INFO:
                    loggerProxy = new InfoLoggerProxy(this.logger);
                    break;
                case WARN:
                    loggerProxy = new WarnLoggerProxy(this.logger);
                    break;
                case ERROR:
                    loggerProxy = new ErrorLoggerProxy(this.logger);
                    break;
                default:
                case DEBUG:
                    loggerProxy = new DebugLoggerProxy(this.logger);
                    break;
            }
            return new Log4JMetricsReporter(this.registry, loggerProxy, this.marker, this.prefix, this.rateUnit, this.durationUnit, this.filter, this.executor, this.shutdownExecutorOnStop);
        }
    }

    private final LoggerProxy loggerProxy;
    private final Marker marker;
    private final String prefix;

    private Log4JMetricsReporter(final MetricRegistry registry,
                                 final LoggerProxy loggerProxy,
                                 final Marker marker,
                                 final String prefix,
                                 final TimeUnit rateUnit,
                                 final TimeUnit durationUnit,
                                 final MetricFilter filter,
                                 final ScheduledExecutorService executor,
                                 final boolean shutdownExecutorOnStop) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.prefix = prefix;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(final SortedMap<String, Gauge> gauges,
                       final SortedMap<String, Counter> counters,
                       final SortedMap<String, Histogram> histograms,
                       final SortedMap<String, Meter> meters,
                       final SortedMap<String, Timer> timers) {
        if (this.loggerProxy.isEnabled(this.marker)) {
            for (final Map.Entry<String, Gauge> entry : gauges.entrySet())
                this.logGauge(entry.getKey(), entry.getValue());

            for (final Map.Entry<String, Counter> entry : counters.entrySet())
                this.logCounter(entry.getKey(), entry.getValue());

            for (final Map.Entry<String, Histogram> entry : histograms.entrySet())
                this.logHistogram(entry.getKey(), entry.getValue());

            for (final Map.Entry<String, Meter> entry : meters.entrySet())
                this.logMeter(entry.getKey(), entry.getValue());

            for (final Map.Entry<String, Timer> entry : timers.entrySet())
                this.logTimer(entry.getKey(), entry.getValue());
        }
    }

    private void logTimer(final String name, final Timer timer) {
        Snapshot snapshot = timer.getSnapshot();
        this.loggerProxy.log(this.marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, " +
                        "p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, " +
                        "m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
            this.prefix(name),
                timer.getCount(),
            this.convertDuration(snapshot.getMin()),
            this.convertDuration(snapshot.getMax()),
            this.convertDuration(snapshot.getMean()),
            this.convertDuration(snapshot.getStdDev()),
            this.convertDuration(snapshot.getMedian()),
            this.convertDuration(snapshot.get75thPercentile()),
            this.convertDuration(snapshot.get95thPercentile()),
            this.convertDuration(snapshot.get98thPercentile()),
            this.convertDuration(snapshot.get99thPercentile()),
            this.convertDuration(snapshot.get999thPercentile()),
            this.convertRate(timer.getMeanRate()),
            this.convertRate(timer.getOneMinuteRate()),
            this.convertRate(timer.getFiveMinuteRate()),
            this.convertRate(timer.getFifteenMinuteRate()),
            this.getRateUnit(),
            this.getDurationUnit());
    }

    private void logMeter(final String name, final Meter meter) {
        this.loggerProxy.log(this.marker,
                "type={}, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
            this.prefix(name),
                meter.getCount(),
            this.convertRate(meter.getMeanRate()),
            this.convertRate(meter.getOneMinuteRate()),
            this.convertRate(meter.getFiveMinuteRate()),
            this.convertRate(meter.getFifteenMinuteRate()),
            this.getRateUnit());
    }

    private void logHistogram(final String name, final Histogram histogram) {
        Snapshot snapshot = histogram.getSnapshot();
        this.loggerProxy.log(this.marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, " +
                        "median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
            this.prefix(name),
                histogram.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getStdDev(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile());
    }

    private void logCounter(final String name, final Counter counter) {
        this.loggerProxy.log(this.marker, "type={}, name={}, count={}", "COUNTER", this.prefix(name), counter.getCount());
    }

    private void logGauge(final String name, final Gauge<?> gauge) {
        this.loggerProxy.log(this.marker, "type={}, name={}, value={}", "GAUGE", this.prefix(name), gauge.getValue());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    private String prefix(final String... components) {
        return MetricRegistry.name(this.prefix, components);
    }

    /* private class to allow logger configuration */
    abstract static class LoggerProxy {
        protected final Logger logger;

        protected LoggerProxy(final Logger logger) {
            this.logger = logger;
        }

        abstract void log(Marker marker, String format, Object... arguments);

        abstract boolean isEnabled(Marker marker);
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

}

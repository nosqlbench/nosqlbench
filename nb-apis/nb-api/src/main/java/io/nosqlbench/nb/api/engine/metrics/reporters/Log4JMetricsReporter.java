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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBCreators;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBFinders;
import io.nosqlbench.nb.api.components.core.PeriodicTaskComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;
import org.apache.logging.log4j.Marker;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a Log4J targeted metrics logging reporter, derived from
 * {@link Slf4jReporter}. This implementation
 * was built to allow for consolidating internal logging dependencies
 * to log4j only.
 */
public class Log4JMetricsReporter extends PeriodicTaskComponent {

    public enum LoggingLevel { TRACE, DEBUG, INFO, WARN, ERROR }
    private final NBCreators.LoggerProxy loggerProxy;
    private final Marker marker;
    private final TimeUnit rateUnit = TimeUnit.NANOSECONDS;
    private final TimeUnit durationUnit = TimeUnit.NANOSECONDS;
    private final long durationFactor = TimeUnit.NANOSECONDS.toNanos(1);
    private final long rateFactor = TimeUnit.NANOSECONDS.toSeconds(1);

    public Log4JMetricsReporter(final NBComponent component,
                                final NBCreators.LoggerProxy loggerProxy,
                                final Marker marker,
                                final MetricFilter filter,
                                final NBLabels extraLabels,
                                final long millis,
                                final boolean oneLastTime) {
        super(component, extraLabels, millis, "REPORT-LOG4J", FirstReport.OnInterval, LastReport.OnInterrupt);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
    }

    @Override
    protected void task() {
        report(NBFinders.allMetricsWithType(NBMetricGauge.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricCounter.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricHistogram.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricMeter.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricTimer.class, getParent())
        );
    }


    public void report( List<NBMetricGauge> gauges,
                        List<NBMetricCounter> counters,
                        List<NBMetricHistogram> histograms,
                        List<NBMetricMeter> meters,
                        List<NBMetricTimer> timers) {
        if (this.loggerProxy.isEnabled(this.marker)) {
            for (NBMetricGauge gauge : gauges)
                this.logGauge(gauge.getLabels().linearizeAsMetrics(), gauge);

            for (NBMetricCounter counter : counters)
                this.logCounter(counter.getLabels().linearizeAsMetrics(), counter);

            for (NBMetricHistogram histogram : histograms)
                this.logHistogram(histogram.getLabels().linearizeAsMetrics(), histogram);

            for (NBMetricMeter meter : meters)
                this.logMeter(meter.getLabels().linearizeAsMetrics(), meter);

            for (NBMetricTimer timer : timers)
                this.logTimer(timer.getLabels().linearizeAsMetrics(), timer);
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
            this.durationUnit);
    }

    protected double convertDuration(double duration) {
        return duration / durationFactor;
    }
    protected double convertRate(double rate) {
        return rate * rateFactor;
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

    protected String getRateUnit() {
        return "events/" + rateUnit;
    }

    private String prefix(final String... components) {
        return NBLabels.forKV((Object[]) components).linearizeAsMetrics();
    }

}

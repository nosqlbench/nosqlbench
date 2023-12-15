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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.Timer;
import com.codahale.metrics.*;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBFinders;
import io.nosqlbench.nb.api.components.core.PeriodicTaskComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsoleReporter extends PeriodicTaskComponent {
    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale = Locale.US;
    private final Clock clock = Clock.defaultClock();
    private final DateFormat dateFormat;
    private final Set<MetricAttribute> disabledMetricAttributes;
    private final String rateUnit;
    private final long rateFactor;
    private final String durationUnit = TimeUnit.NANOSECONDS.toString().toLowerCase(Locale.US);
    private final long durationFactor = TimeUnit.NANOSECONDS.toNanos(1);

    public ConsoleReporter(NBComponent node, NBLabels extraLabels, long millis, boolean oneLastTime,
                           PrintStream output, Set<MetricAttribute> disabledMetricAttributes) {
        super(node, extraLabels, millis, oneLastTime, "REPORT-CONSOLE");
        this.output = output;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.MEDIUM,
            locale);
        dateFormat.setTimeZone(TimeZone.getDefault());
        this.disabledMetricAttributes = disabledMetricAttributes;
        String s = TimeUnit.NANOSECONDS.toString().toLowerCase(Locale.US);
        rateUnit = s.substring(0, s.length() - 1);
        this.rateFactor = TimeUnit.NANOSECONDS.toSeconds(1);
    }

    @Override
    protected void task() {
        report();
    }

    public void report() {
        report(NBFinders.allMetricsWithType(NBMetricGauge.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricCounter.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricHistogram.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricMeter.class, getParent()),
            NBFinders.allMetricsWithType(NBMetricTimer.class, getParent())
        );
    }

    public void report(List<NBMetricGauge> gauges,
                       List<NBMetricCounter> counters,
                       List<NBMetricHistogram> histograms,
                       List<NBMetricMeter> meters,
                       List<NBMetricTimer> timers) {
        final String dateTime = dateFormat.format(new Date(clock.getTime()));
        printWithBanner(dateTime, '=');
        output.println();

        if (!gauges.isEmpty()) {
            printWithBanner("-- Gauges", '-');
            for (NBMetricGauge gauge : gauges) {
                output.println(gauge.getLabels().linearizeAsMetrics());
                printGauge(gauge);
            }
            output.println();
        }

        if (!counters.isEmpty()) {
            printWithBanner("-- Counters", '-');
            for (NBMetricCounter counter : counters) {
                output.println(counter.getLabels().linearizeAsMetrics());
                printCounter(counter);
            }
            output.println();
        }

        if (!histograms.isEmpty()) {
            printWithBanner("-- Histograms", '-');
            for (NBMetricHistogram histogram : histograms) {
                output.println(histogram.getLabels().linearizeAsMetrics());
                printHistogram(histogram);
            }
            output.println();
        }

        if (!meters.isEmpty()) {
            printWithBanner("-- Meters", '-');
            for (NBMetricMeter meter : meters) {
                output.println(meter.getLabels().linearizeAsMetrics());
                printMeter(meter);
            }
            output.println();
        }

        if (!timers.isEmpty()) {
            printWithBanner("-- Timers", '-');
            for (NBMetricTimer timer : timers) {
                output.println(timer.getLabels().linearizeAsMetrics());
                printTimer(timer);
            }
            output.println();
        }

        output.println();
        output.flush();
    }

    private void printMeter(Meter meter) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", meter.getCount()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f events/%s", convertRate(meter.getMeanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f events/%s", convertRate(meter.getOneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f events/%s", convertRate(meter.getFiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f events/%s", convertRate(meter.getFifteenMinuteRate()), getRateUnit()));
    }

    private void printCounter(Counter counter) {
        output.printf(locale, "             count = %d%n", counter.getCount());
    }

    private void printGauge(Gauge<?> gauge) {
        output.printf(locale, "             value = %s%n", gauge.getValue());
    }

    private void printHistogram(Histogram histogram) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", histogram.getCount()));
        Snapshot snapshot = histogram.getSnapshot();
        printIfEnabled(MetricAttribute.MIN, String.format(locale, "               min = %d", snapshot.getMin()));
        printIfEnabled(MetricAttribute.MAX, String.format(locale, "               max = %d", snapshot.getMax()));
        printIfEnabled(MetricAttribute.MEAN, String.format(locale, "              mean = %2.2f", snapshot.getMean()));
        printIfEnabled(MetricAttribute.STDDEV, String.format(locale, "            stddev = %2.2f", snapshot.getStdDev()));
        printIfEnabled(MetricAttribute.P50, String.format(locale, "            median = %2.2f", snapshot.getMedian()));
        printIfEnabled(MetricAttribute.P75, String.format(locale, "              75%% <= %2.2f", snapshot.get75thPercentile()));
        printIfEnabled(MetricAttribute.P95, String.format(locale, "              95%% <= %2.2f", snapshot.get95thPercentile()));
        printIfEnabled(MetricAttribute.P98, String.format(locale, "              98%% <= %2.2f", snapshot.get98thPercentile()));
        printIfEnabled(MetricAttribute.P99, String.format(locale, "              99%% <= %2.2f", snapshot.get99thPercentile()));
        printIfEnabled(MetricAttribute.P999, String.format(locale, "            99.9%% <= %2.2f", snapshot.get999thPercentile()));
    }

    private void printTimer(Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", timer.getCount()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f calls/%s", convertRate(timer.getMeanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f calls/%s", convertRate(timer.getOneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f calls/%s", convertRate(timer.getFiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f calls/%s", convertRate(timer.getFifteenMinuteRate()), getRateUnit()));

        printIfEnabled(MetricAttribute.MIN, String.format(locale, "               min = %2.2f %s", convertDuration(snapshot.getMin()), getDurationUnit()));
        printIfEnabled(MetricAttribute.MAX, String.format(locale, "               max = %2.2f %s", convertDuration(snapshot.getMax()), getDurationUnit()));
        printIfEnabled(MetricAttribute.MEAN, String.format(locale, "              mean = %2.2f %s", convertDuration(snapshot.getMean()), getDurationUnit()));
        printIfEnabled(MetricAttribute.STDDEV, String.format(locale, "            stddev = %2.2f %s", convertDuration(snapshot.getStdDev()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P50, String.format(locale, "            median = %2.2f %s", convertDuration(snapshot.getMedian()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P75, String.format(locale, "              75%% <= %2.2f %s", convertDuration(snapshot.get75thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P95, String.format(locale, "              95%% <= %2.2f %s", convertDuration(snapshot.get95thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P98, String.format(locale, "              98%% <= %2.2f %s", convertDuration(snapshot.get98thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P99, String.format(locale, "              99%% <= %2.2f %s", convertDuration(snapshot.get99thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P999, String.format(locale, "            99.9%% <= %2.2f %s", convertDuration(snapshot.get999thPercentile()), getDurationUnit()));
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }

    /**
     * Print only if the attribute is enabled
     *
     * @param type
     *     Metric attribute
     * @param status
     *     Status to be logged
     */
    private void printIfEnabled(MetricAttribute type, String status) {
        if (getDisabledMetricAttributes().contains(type)) {
            return;
        }

        output.println(status);
    }

    private Set<MetricAttribute> getDisabledMetricAttributes() {
        return disabledMetricAttributes;
    }

    protected String getRateUnit() {
        return rateUnit;
    }

    protected double convertRate(double rate) {
        return rate * rateFactor;
    }

    protected String getDurationUnit() {
        return durationUnit;
    }

    protected double convertDuration(double duration) {
        return duration / durationFactor;
    }

    public void reportOnce(List<NBMetric> summaryMetrics) {
        List<NBMetricGauge> gauges = new ArrayList<>();
        List<NBMetricCounter> counters = new ArrayList<>();
        List<NBMetricHistogram> histograms = new ArrayList<>();
        List<NBMetricMeter> meters = new ArrayList<>();
        List<NBMetricTimer> timers = new ArrayList<>();
        for (NBMetric metric : summaryMetrics) {
            if (metric instanceof NBMetricGauge) {
                gauges.add((NBMetricGauge) metric);
            }
            if (metric instanceof NBMetricCounter) {
                counters.add((NBMetricCounter) metric);
            }
            if (metric instanceof NBMetricHistogram) {
                histograms.add((NBMetricHistogram) metric);
            }
            if (metric instanceof NBMetricMeter) {
                meters.add((NBMetricMeter) metric);
            }
            if (metric instanceof NBMetricTimer) {
                timers.add((NBMetricTimer) metric);
            }
        }
        report(gauges, counters, histograms, meters, timers);
    }

    public void reportCountsOnce(List<NBMetric> summaryMetrics) {
        // TODO: implement counts only renderer
        // TODO: resolve ambiguity around reporting counts only or reporting nothing for short sessions
    }
}

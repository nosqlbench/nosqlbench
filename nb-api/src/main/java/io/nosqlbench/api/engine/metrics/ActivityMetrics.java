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

package io.nosqlbench.api.engine.metrics;

import com.codahale.metrics.*;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.engine.activityapi.core.MetricRegistryService;
import io.nosqlbench.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public enum ActivityMetrics {
    ;

    private static final Logger logger = LogManager.getLogger(ActivityMetrics.class);

    public static final String HDRDIGITS_PARAM = "hdr_digits";
    public static final int DEFAULT_HDRDIGITS = 4;
    private static int _HDRDIGITS = ActivityMetrics.DEFAULT_HDRDIGITS;

    private static MetricRegistry registry;

    public static MetricFilter METRIC_FILTER = (name, metric) -> {
        return true;
    };
    private static final List<MetricsCloseable> metricsCloseables = new ArrayList<>();


    public static int getHdrDigits() {
        return ActivityMetrics._HDRDIGITS;
    }

    public static void setHdrDigits(final int hdrDigits) {
        _HDRDIGITS = hdrDigits;
    }

    /**
     * Register a named metric for an activity, synchronized on the activity
     *
     * @param named
     *     The activity def that the metric will be for
     * @param name
     *     The full metric name
     * @param metricProvider
     *     A function to actually create the metric if needed
     * @return a Metric, or null if the metric for the name was already present
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static Metric register(final NBLabeledElement labeled, final String name, final MetricProvider metricProvider) {
        final String fullMetricName = labeled.linearizedByValueGraphite("name", name);
        Metric metric = ActivityMetrics.get().getMetrics().get(fullMetricName);
        if (null == metric) synchronized (labeled) {
            metric = ActivityMetrics.get().getMetrics().get(fullMetricName);
            if (null == metric) {
                metric = metricProvider.getMetric();
                final Metric registered = ActivityMetrics.get().register(fullMetricName, metric);
                ActivityMetrics.logger.debug(() -> "registered metric: " + fullMetricName);
                return registered;
            }
        }
        return metric;
    }

    /**
     * <p>Create a timer associated with an activity.</p>
     *
     * <p>If the provide ActivityDef contains a parameter "hdr_digits", then it will be used to set the number of
     * significant digits on the histogram's precision.</p>
     *
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param named
     *     an associated activity def
     * @param name
     *     a simple, descriptive name for the timer
     * @return the timer, perhaps a different one if it has already been registered
     */
    public static Timer timer(final NBLabeledElement labeled, final String name, final int hdrdigits) {
        final String fullMetricName = labeled.linearizedByValueGraphite("name", name);
        final Timer registeredTimer = (Timer) ActivityMetrics.register(labeled, name, () ->
            new NicerTimer(fullMetricName,
                new DeltaHdrHistogramReservoir(
                    fullMetricName,
                    hdrdigits
                )
            ));
        return registeredTimer;
    }

    public static Timer timer(final String fullMetricName) {
        final NicerTimer timer = ActivityMetrics.get().register(fullMetricName, new NicerTimer(
            fullMetricName,
            new DeltaHdrHistogramReservoir(
                fullMetricName,
                ActivityMetrics._HDRDIGITS
            ))
        );
        return timer;
    }

    /**
     * <p>Create an HDR histogram associated with an activity.</p>
     *
     * <p>If the provide ActivityDef contains a parameter "hdr_digits", then it will be used to set the number of
     * significant digits on the histogram's precision.</p>
     *
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param named
     *     an associated activity def
     * @param name
     *     a simple, descriptive name for the histogram
     * @return the histogram, perhaps a different one if it has already been registered
     */
    public static Histogram histogram(final NBLabeledElement labeled, final String name, final int hdrdigits) {
        final String fullMetricName = labeled.linearizedByValueGraphite("name", name);
        return (Histogram) ActivityMetrics.register(labeled, name, () ->
            new NicerHistogram(
                fullMetricName,
                new DeltaHdrHistogramReservoir(
                    fullMetricName,
                    hdrdigits
                )
            ));
    }

    public static Histogram histogram(final String fullname) {
        final NicerHistogram histogram = ActivityMetrics.get().register(fullname, new NicerHistogram(
            fullname,
            new DeltaHdrHistogramReservoir(
                fullname,
                ActivityMetrics._HDRDIGITS
            )
        ));
        return histogram;
    }

    /**
     * <p>Create a counter associated with an activity.</p>
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param named
     *     an associated activity def
     * @param name
     *     a simple, descriptive name for the counter
     * @return the counter, perhaps a different one if it has already been registered
     */
    public static Counter counter(final NBLabeledElement parent, final String submetricName) {
        final Map<String, String> metricLabels = parent.getLabelsAnd("name", submetricName);
        return (Counter) ActivityMetrics.register(parent, submetricName, Counter::new);
        // {"__name__"=$name}
    }

    /**
     * <p>Create a meter associated with an activity.</p>
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param named
     *     an associated activity def
     * @param name
     *     a simple, descriptive name for the meter
     * @return the meter, perhaps a different one if it has already been registered
     */
    public static Meter meter(final NBLabeledElement named, final String name) {
        return (Meter) ActivityMetrics.register(named, name, Meter::new);
    }

    private static MetricRegistry get() {
        if (null != registry) return ActivityMetrics.registry;
        synchronized (ActivityMetrics.class) {
            if (null == registry) ActivityMetrics.registry = ActivityMetrics.lookupRegistry();
        }
        return ActivityMetrics.registry;
    }

    @SuppressWarnings("unchecked")
    public static <T> Gauge<T> gauge(final NBLabeledElement named, final String name, final Gauge<T> gauge) {
        return (Gauge<T>) ActivityMetrics.register(named, name, () -> gauge);
    }

    private static MetricRegistry lookupRegistry() {
        final ServiceLoader<MetricRegistryService> metricRegistryServices =
            ServiceLoader.load(MetricRegistryService.class);
        final List<MetricRegistryService> mrss = new ArrayList<>();
        metricRegistryServices.iterator().forEachRemaining(mrss::add);

        if (1 == mrss.size()) return mrss.get(0).getMetricRegistry();
        final String infoMsg = "Unable to load a dynamic MetricRegistry via ServiceLoader, using the default.";
        ActivityMetrics.logger.info(infoMsg);
        return new MetricRegistry();

    }


    public static MetricRegistry getMetricRegistry() {
        return ActivityMetrics.get();
    }

    /**
     * Add a histogram interval logger to matching metrics in this JVM instance.
     *
     * @param sessionName
     *     The name for the session to be annotated in the histogram log
     * @param pattern
     *     A regular expression pattern to filter out metric names for logging
     * @param filename
     *     A file to log the histogram data in
     * @param interval
     *     How many seconds to wait between writing each interval histogram
     */
    public static void addHistoLogger(final String sessionName, final String pattern, String filename, final String interval) {
        if (filename.contains("_SESSION_")) filename = filename.replace("_SESSION_", sessionName);
        final Pattern compiledPattern = Pattern.compile(pattern);
        final File logfile = new File(filename);
        final long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:'" + interval + '\''));

        final HistoIntervalLogger histoIntervalLogger =
            new HistoIntervalLogger(sessionName, logfile, compiledPattern, intervalMillis);
        ActivityMetrics.logger.debug(() -> "attaching " + histoIntervalLogger + " to the metrics registry.");
        ActivityMetrics.get().addListener(histoIntervalLogger);
        ActivityMetrics.metricsCloseables.add(histoIntervalLogger);
    }

    /**
     * Add a histogram stats logger to matching metrics in this JVM instance.
     *
     * @param sessionName
     *     The name for the session to be annotated in the histogram log
     * @param pattern
     *     A regular expression pattern to filter out metric names for logging
     * @param filename
     *     A file to log the histogram data in
     * @param interval
     *     How many seconds to wait between writing each interval histogram
     */
    public static void addStatsLogger(final String sessionName, final String pattern, String filename, final String interval) {
        if (filename.contains("_SESSION_")) filename = filename.replace("_SESSION_", sessionName);
        final Pattern compiledPattern = Pattern.compile(pattern);
        final File logfile = new File(filename);
        final long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + '\''));

        final HistoStatsLogger histoStatsLogger =
            new HistoStatsLogger(sessionName, logfile, compiledPattern, intervalMillis, TimeUnit.NANOSECONDS);
        ActivityMetrics.logger.debug(() -> "attaching " + histoStatsLogger + " to the metrics registry.");
        ActivityMetrics.get().addListener(histoStatsLogger);
        ActivityMetrics.metricsCloseables.add(histoStatsLogger);
    }

    /**
     * Add a classic histogram in addition to the default implementation in this runtime. This is a way to
     * get a view to both the enhanced histogram implementation as well as the classic implementation in the
     * same scenario.
     *
     * @param sessionName
     *     The name of the session to be annotated in the classic histogram
     * @param pattern
     *     A regular expression pattern to filter out metric names for inclusion
     * @param prefix
     *     The name prefix to add to the classic histograms so that they fit into the existing metrics namespace
     * @param interval
     *     How frequently to update the histogram
     */
    public static void addClassicHistos(final String sessionName, final String pattern, final String prefix, final String interval) {
        final Pattern compiledPattern = Pattern.compile(pattern);
        final long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + '\''));

        final ClassicHistoListener classicHistoListener =
            new ClassicHistoListener(ActivityMetrics.get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        ActivityMetrics.logger.debug(() -> "attaching histo listener " + classicHistoListener + " to the metrics registry.");
        ActivityMetrics.get().addListener(classicHistoListener);

        final ClassicTimerListener classicTimerListener =
            new ClassicTimerListener(ActivityMetrics.get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        ActivityMetrics.logger.debug(() -> "attaching timer listener " + classicTimerListener + " to the metrics registry.");
        ActivityMetrics.get().addListener(classicTimerListener);
    }

    /**
     * This should be called at the end of a process, so that open intervals can be finished, logs closed properly,
     * etc.
     *
     * @param showChart
     *     whether to chart metrics on console
     */
    public static void closeMetrics(final boolean showChart) {
        ActivityMetrics.logger.trace("Closing all registered metrics closable objects.");
        for (final MetricsCloseable metricsCloseable : ActivityMetrics.metricsCloseables) {
            ActivityMetrics.logger.trace(() -> "closing metrics closeable: " + metricsCloseable);
            metricsCloseable.closeMetrics();
            if (showChart) metricsCloseable.chart();
        }
    }

    private interface MetricProvider {
        Metric getMetric();
    }

    public static void reportTo(final PrintStream out) {
        out.println("====================  BEGIN-METRIC-LOG  ====================");
        final ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(getMetricRegistry())
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(out)
            .build();
        consoleReporter.report();
        out.println("====================   END-METRIC-LOG   ====================");
    }

    public static void mountSubRegistry(final String mountPrefix, final MetricRegistry subRegistry) {
        new MetricsRegistryMount(ActivityMetrics.getMetricRegistry(), subRegistry, mountPrefix);
    }

    public static void removeActivityMetrics(final NBNamedElement named) {
        ActivityMetrics.get().getMetrics().keySet().stream().filter(s -> s.startsWith(named.getName() + '.'))
            .forEach(ActivityMetrics.get()::remove);
    }

}

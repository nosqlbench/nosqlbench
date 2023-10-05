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
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.NBNamedElement;
import io.nosqlbench.api.labels.NBLabelsFilter;
import io.nosqlbench.api.engine.activityapi.core.MetricRegistryService;
import io.nosqlbench.api.engine.metrics.instruments.*;
import io.nosqlbench.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.regex.Pattern;

public class ActivityMetrics {

    private static final Logger logger = LogManager.getLogger(ActivityMetrics.class);

    public static final String HDRDIGITS_PARAM = "hdr_digits";
    public static final int DEFAULT_HDRDIGITS = 4;
    private static int _HDRDIGITS = DEFAULT_HDRDIGITS;

    private static MetricRegistry registry;

    public static MetricFilter METRIC_FILTER = (name, metric) -> {
        return true;
    };
    private static final List<MetricsCloseable> metricsCloseables = new ArrayList<>();
    private static NBLabelsFilter labelValidator;
    private static NBLabelsFilter labelFilter;


    public static int getHdrDigits() {
        return _HDRDIGITS;
    }

    public static void setHdrDigits(int hdrDigits) {
        ActivityMetrics._HDRDIGITS = hdrDigits;
    }

    /**
     * Register a named metric for an activity, synchronized on the activity
     *
     * @param named
     *     The activity def that the metric will be for
     * @param metricFamilyName
     *     The full metric name
     * @param metricProvider
     *     A function to actually create the metric if needed
     * @return a Metric, or null if the metric for the name was already present
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static Metric register(NBLabels labels, MetricProvider metricProvider) {

        labels = labelFilter != null ? labelFilter.apply(labels) : labels;
        labels = labelValidator != null ? labelValidator.apply(labels) : labels;

        final String graphiteName = labels.linearizeValues('.', "[activity]", "[space]", "[op]", "name");
        Metric metric = get().getMetrics().get(graphiteName);

        if (null == metric) {
            synchronized (labels) {
                metric = get().getMetrics().get(graphiteName);
                if (null == metric) {
                    metric = metricProvider.getMetric();
                    Metric registered = get().register(graphiteName, metric);
                    logger.debug(() -> "registered metric: " + graphiteName);
                    return registered;
                }
            }
        }
        return metric;
    }

    /**
     * Calls to this version of register must be done with a pre-built metric instrument.
     * This means that it is not suitable for lazily creating metric objects directly on
     * instances which are one of many. Instead, call this to register metrics at the start
     * of an owning element.
     *
     * This version of register expects that you have fully labeled a metric, including
     * addint the 'name' field, also known as the <em>metric family name</em> in some specifications.
     *
     * It is due to be replaced by a different registry format soon.
     *
     * @param labeledMetric
     * @return the metric instance
     */
    public static <M extends NBLabeledMetric> M register(M labeledMetric) {
        NBLabels labels = labeledMetric.getLabels();
        labels = labelFilter != null ? labelFilter.apply(labels) : labels;
        labels = labelValidator != null ? labelValidator.apply(labels) : labels;

        final String graphiteName = labels.linearizeValues('.', "[activity]", "[space]", "[op]", "name");
//        String sanitized = sanitize(graphiteName);
//        if (!graphiteName.equals(sanitized)) {
//            throw new RuntimeException("Attempted to register a metric which was not compatible with labeled metric forms. Submitted as '" + graphiteName + "', but should likely be '" + sanitized + "'");
//        }
        Metric metric = get().getMetrics().get(graphiteName);

        metric = get().getMetrics().get(graphiteName);
        if (metric!=null) {
            logger.warn("Metric already registered for '" + graphiteName + "', probably logic error which could invalidate metric values.");
        } else {
            get().register(graphiteName, labeledMetric);
        }
        return labeledMetric;
    }

    public static void unregister(NBLabeledElement element) {
        final String graphiteName = element.getLabels().linearizeValues('.', "[activity]", "[space]", "[op]", "name");
        if (!get().getMetrics().containsKey(graphiteName)) {
            logger.warn("Removing non-extant metric by name: '"+ graphiteName + "'");
        }
        get().remove(graphiteName);
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
     * @param metricFamilyName
     *     a simple, descriptive name for the timer
     * @return the timer, perhaps a different one if it has already been registered
     */
    public static Timer timer(NBLabeledElement parent, String metricFamilyName, int hdrdigits) {
        final NBLabels labels = parent.getLabels().and("name", sanitize(metricFamilyName));


        Timer registeredTimer = (Timer) register(labels, () ->
            new NBMetricTimer(labels,
                new DeltaHdrHistogramReservoir(
                    labels,
                    hdrdigits
                )
            ));
        return registeredTimer;
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
     * @param metricFamilyName
     *     a simple, descriptive name for the histogram
     * @return the histogram, perhaps a different one if it has already been registered
     */
    public static Histogram histogram(NBLabeledElement labeled, String metricFamilyName, int hdrdigits) {
        final NBLabels labels = labeled.getLabels().and("name", sanitize(metricFamilyName));
        return (Histogram) register(labels, () ->
            new NBMetricHistogram(
                labels,
                new DeltaHdrHistogramReservoir(
                    labels,
                    hdrdigits
                )
            ));
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
    public static Counter counter(NBLabeledElement parent, String metricFamilyName) {
        final NBLabels labels = parent.getLabels().and("name", metricFamilyName);
        return (Counter) register(labels, () -> new NBMetricCounter(labels));
    }

    /**
     * <p>Create a meter associated with an activity.</p>
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param named
     *     an associated activity def
     * @param metricFamilyName
     *     a simple, descriptive name for the meter
     * @return the meter, perhaps a different one if it has already been registered
     */
    public static Meter meter(NBLabeledElement parent, String metricFamilyName) {
        final NBLabels labels = parent.getLabels().and("name", sanitize(metricFamilyName));
        return (Meter) register(labels, () -> new NBMetricMeter(labels));
    }

    private static MetricRegistry get() {
        if (null != ActivityMetrics.registry) {
            return registry;
        }
        synchronized (ActivityMetrics.class) {
            if (null == ActivityMetrics.registry) {
                registry = lookupRegistry();
            }
        }
        return registry;
    }

    /**
     * This variant creates a named metric for all of the stats which may be needed, name with metricname_average,
     * and so on. It uses the same data reservoir for all views, but only returns one of them as a handle to the metric.
     * This has the effect of leaving some of the metric objects unreferencable from the caller side. This may need
     * to be changed in a future update in the even that full inventory management is required on metric objects here.
     *
     * @param parent
     *     The labeled element the metric pertains to
     * @param metricFamilyName
     *     The name of the measurement
     * @return One of the created metrics, suitable for calling {@link DoubleSummaryGauge#accept(double)} on.
     */
    public static DoubleSummaryGauge summaryGauge(NBLabeledElement parent, String metricFamilyName) {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        DoubleSummaryGauge anyGauge = null;
        for (DoubleSummaryGauge.Stat statName : DoubleSummaryGauge.Stat.values()) {
            final NBLabels labels = parent.getLabels()
                .and("name", sanitize(metricFamilyName))
                .modifyValue("name", n -> n + "_" + statName.name().toLowerCase());
            anyGauge = (DoubleSummaryGauge) register(labels, () -> new DoubleSummaryGauge(labels, statName, stats));
        }
        return anyGauge;
    }


    public static NBMetricGauge gauge(NBMetricGauge gauge) {
        final NBLabels labels = gauge.getLabels();
        return (NBMetricGauge) register(labels, () -> new NBMetricGaugeWrapper(labels, gauge));

    }

    @SuppressWarnings("unchecked")
    public static Gauge<Double> gauge(NBLabeledElement parent, String metricFamilyName, Gauge<Double> gauge) {
        final NBLabels labels = parent.getLabels().and("name", sanitize(metricFamilyName));
        return (Gauge<Double>) register(labels, () -> new NBMetricGaugeWrapper(labels, gauge));
    }

    private static MetricRegistry lookupRegistry() {
        ServiceLoader<MetricRegistryService> metricRegistryServices =
            ServiceLoader.load(MetricRegistryService.class);
        List<MetricRegistryService> mrss = new ArrayList<>();
        metricRegistryServices.iterator().forEachRemaining(mrss::add);

        if (1 == mrss.size()) {
            return mrss.get(0).getMetricRegistry();
        }
        final String infoMsg = "Unable to load a dynamic MetricRegistry via ServiceLoader, using the default.";
        logger.info(infoMsg);
        return new MetricRegistry();

    }


    public static MetricRegistry getMetricRegistry() {
        return get();
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
    public static void addHistoLogger(String sessionName, String pattern, String filename, String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", sessionName);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:'" + interval + '\''));

        HistoIntervalLogger histoIntervalLogger =
            new HistoIntervalLogger(sessionName, logfile, compiledPattern, intervalMillis);
        logger.debug(() -> "attaching " + histoIntervalLogger + " to the metrics registry.");
        get().addListener(histoIntervalLogger);
        metricsCloseables.add(histoIntervalLogger);
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
    public static void addStatsLogger(String sessionName, String pattern, String filename, String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", sessionName);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + '\''));

        HistoStatsLogger histoStatsLogger =
            new HistoStatsLogger(sessionName, logfile, compiledPattern, intervalMillis, TimeUnit.NANOSECONDS);
        logger.debug(() -> "attaching " + histoStatsLogger + " to the metrics registry.");
        get().addListener(histoStatsLogger);
        metricsCloseables.add(histoStatsLogger);
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
    public static void addClassicHistos(String sessionName, String pattern, String prefix, String interval) {
        Pattern compiledPattern = Pattern.compile(pattern);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + '\''));

        ClassicHistoListener classicHistoListener =
            new ClassicHistoListener(get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        logger.debug(() -> "attaching histo listener " + classicHistoListener + " to the metrics registry.");
        get().addListener(classicHistoListener);

        ClassicTimerListener classicTimerListener =
            new ClassicTimerListener(get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        logger.debug(() -> "attaching timer listener " + classicTimerListener + " to the metrics registry.");
        get().addListener(classicTimerListener);
    }

    /**
     * This should be called at the end of a process, so that open intervals can be finished, logs closed properly,
     * etc.
     *
     * @param showChart
     *     whether to chart metrics on console
     */
    public static void closeMetrics() {
        logger.trace("Closing all registered metrics closable objects.");
        for (MetricsCloseable metricsCloseable : metricsCloseables) {
            logger.trace(() -> "closing metrics closeable: " + metricsCloseable);
            metricsCloseable.closeMetrics();
        }
    }

    public static void setLabelValidator(String annotateLabelSpec) {
        labelValidator = new NBLabelsFilter(annotateLabelSpec);
        labelFilter = new NBLabelsFilter(annotateLabelSpec);
    }

    private interface MetricProvider {
        Metric getMetric();
    }

    public static void reportTo(PrintStream out) {
        out.println("====================  BEGIN-METRIC-LOG  ====================");
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(out)
            .build();
        consoleReporter.report();
        out.println("====================   END-METRIC-LOG   ====================");
    }

    public static void mountSubRegistry(String mountPrefix, MetricRegistry subRegistry) {
        new MetricsRegistryMount(getMetricRegistry(), subRegistry, mountPrefix);
    }

    public static void removeActivityMetrics(NBNamedElement named) {
        get().getMetrics().keySet().stream().filter(s -> s.startsWith(named.getName() + '.'))
            .forEach(get()::remove);
    }


    public static String sanitize(String word) {
        String sanitized = word;
        sanitized = sanitized.replaceAll("\\.", "__");
        sanitized = sanitized.replaceAll("-", "_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]+", "");

        if (!word.equals(sanitized)) {
            logger.warn("The identifier or value '" + word + "' was sanitized to '" + sanitized + "' to be compatible with monitoring systems. You should probably change this to make diagnostics easier.");
        }
        return sanitized;
    }

}

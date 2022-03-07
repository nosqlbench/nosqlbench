/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.*;
import io.nosqlbench.engine.api.activityapi.core.MetricRegistryService;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.util.Unit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.script.ScriptContext;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ActivityMetrics {

    private final static Logger logger = LogManager.getLogger(ActivityMetrics.class);

    public static final String HDRDIGITS_PARAM = "hdr_digits";
    public static final int DEFAULT_HDRDIGITS = 4;
    private static int _HDRDIGITS = DEFAULT_HDRDIGITS;

    private static MetricRegistry registry;

    public static MetricFilter METRIC_FILTER = (name, metric) -> {
        return true;
    };
    private static final List<MetricsCloseable> metricsCloseables = new ArrayList<>();


    public static int getHdrDigits() {
        return _HDRDIGITS;
    }

    public static void setHdrDigits(int hdrDigits) {
        ActivityMetrics._HDRDIGITS = hdrDigits;
    }

    private ActivityMetrics() {
    }

    /**
     * Register a named metric for an activity, synchronized on the activity
     *
     * @param activityDef    The activity def that the metric will be for
     * @param name           The full metric name
     * @param metricProvider A function to actually create the metric if needed
     * @return a Metric, or null if the metric for the name was already present
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static Metric register(ActivityDef activityDef, String name, MetricProvider metricProvider) {
        String fullMetricName = activityDef.getAlias() + "." + name;
        Metric metric = get().getMetrics().get(fullMetricName);
        if (metric == null) {
            synchronized (activityDef) {
                metric = get().getMetrics().get(fullMetricName);
                if (metric == null) {
                    metric = metricProvider.getMetric();
                    return get().register(fullMetricName, metric);
                }
            }
        }
        return metric;
    }

    private static Metric register(ScriptContext context, String name, MetricProvider metricProvider) {
        Metric metric = get().getMetrics().get(name);
        if (metric == null) {
            synchronized (context) {
                metric = get().getMetrics().get(name);
                if (metric == null) {
                    metric = metricProvider.getMetric();
                    Metric registered = get().register(name, metric);
                    logger.info("registered scripting metric: " + name);
                    return registered;
                }
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
     * @param activityDef an associated activity def
     * @param name        a simple, descriptive name for the timer
     * @return the timer, perhaps a different one if it has already been registered
     */
    public static Timer timer(ActivityDef activityDef, String name) {
        String fullMetricName = activityDef.getAlias() + "." + name;
        Timer registeredTimer = (Timer) register(activityDef, name, () ->
            new NicerTimer(fullMetricName,
                new DeltaHdrHistogramReservoir(
                    fullMetricName,
                    activityDef.getParams().getOptionalInteger(HDRDIGITS_PARAM).orElse(_HDRDIGITS)
                )
            ));
        return registeredTimer;
    }

    public static Timer timer(String fullMetricName) {
        NicerTimer timer = get().register(fullMetricName, new NicerTimer(
            fullMetricName,
            new DeltaHdrHistogramReservoir(
                fullMetricName,
                _HDRDIGITS
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
     * @param activityDef an associated activity def
     * @param name        a simple, descriptive name for the histogram
     * @return the histogram, perhaps a different one if it has already been registered
     */
    public static Histogram histogram(ActivityDef activityDef, String name) {
        String fullMetricName = activityDef.getAlias() + "." + name;
        return (Histogram) register(activityDef, name, () ->
            new NicerHistogram(
                fullMetricName,
                new DeltaHdrHistogramReservoir(
                    fullMetricName,
                    activityDef.getParams().getOptionalInteger(HDRDIGITS_PARAM).orElse(_HDRDIGITS)
                )
            ));
    }

    public static Histogram histogram(String fullname) {
        NicerHistogram histogram = get().register(fullname, new NicerHistogram(
            fullname,
            new DeltaHdrHistogramReservoir(
                fullname,
                _HDRDIGITS
            )
        ));
        return histogram;
    }

    /**
     * <p>Create a counter associated with an activity.</p>
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param activityDef an associated activity def
     * @param name        a simple, descriptive name for the counter
     * @return the counter, perhaps a different one if it has already been registered
     */
    public static Counter counter(ActivityDef activityDef, String name) {
        return (Counter) register(activityDef, name, Counter::new);
    }

    /**
     * <p>Create a meter associated with an activity.</p>
     * <p>This method ensures that if multiple threads attempt to create the same-named metric on a given activity,
     * that only one of them succeeds.</p>
     *
     * @param activityDef an associated activity def
     * @param name        a simple, descriptive name for the meter
     * @return the meter, perhaps a different one if it has already been registered
     */
    public static Meter meter(ActivityDef activityDef, String name) {
        return (Meter) register(activityDef, name, Meter::new);
    }

    private static MetricRegistry get() {
        if (registry != null) {
            return registry;
        }
        synchronized (ActivityMetrics.class) {
            if (registry == null) {
                registry = lookupRegistry();
            }
        }
        return registry;
    }

    @SuppressWarnings("unchecked")
    public static <T> Gauge<T> gauge(ActivityDef activityDef, String name, Gauge<T> gauge) {
        return (Gauge<T>) register(activityDef, name, () -> gauge);
    }

    @SuppressWarnings("unchecked")
    public static <T> Gauge<T> gauge(ScriptContext scriptContext, String name, Gauge<T> gauge) {
        return (Gauge<T>) register(scriptContext, name, () -> gauge);
    }


    private static MetricRegistry lookupRegistry() {
        ServiceLoader<MetricRegistryService> metricRegistryServices =
            ServiceLoader.load(MetricRegistryService.class);
        List<MetricRegistryService> mrss = new ArrayList<>();
        metricRegistryServices.iterator().forEachRemaining(mrss::add);

        if (mrss.size() == 1) {
            return mrss.get(0).getMetricRegistry();
        } else {
            String infoMsg = "Unable to load a dynamic MetricRegistry via ServiceLoader, using the default.";
            logger.info(infoMsg);
            return new MetricRegistry();
        }

    }


    public static MetricRegistry getMetricRegistry() {
        return get();
    }

    /**
     * Add a histogram interval logger to matching metrics in this JVM instance.
     *
     * @param sessionName The name for the session to be annotated in the histogram log
     * @param pattern     A regular expression pattern to filter out metric names for logging
     * @param filename    A file to log the histogram data in
     * @param interval    How many seconds to wait between writing each interval histogram
     */
    public static void addHistoLogger(String sessionName, String pattern, String filename, String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", sessionName);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:'" + interval + "'"));

        HistoIntervalLogger histoIntervalLogger =
            new HistoIntervalLogger(sessionName, logfile, compiledPattern, intervalMillis);
        logger.debug("attaching " + histoIntervalLogger + " to the metrics registry.");
        get().addListener(histoIntervalLogger);
        metricsCloseables.add(histoIntervalLogger);
    }

    /**
     * Add a histogram stats logger to matching metrics in this JVM instance.
     *
     * @param sessionName The name for the session to be annotated in the histogram log
     * @param pattern     A regular expression pattern to filter out metric names for logging
     * @param filename    A file to log the histogram data in
     * @param interval    How many seconds to wait between writing each interval histogram
     */
    public static void addStatsLogger(String sessionName, String pattern, String filename, String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", sessionName);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + "'"));

        HistoStatsLogger histoStatsLogger =
            new HistoStatsLogger(sessionName, logfile, compiledPattern, intervalMillis, TimeUnit.NANOSECONDS);
        logger.debug("attaching " + histoStatsLogger + " to the metrics registry.");
        get().addListener(histoStatsLogger);
        metricsCloseables.add(histoStatsLogger);
    }

    /**
     * Add a classic histogram in addition to the default implementation in this runtime. This is a way to
     * get a view to both the enhanced histogram implementation as well as the classic implementation in the
     * same scenario.
     *
     * @param sessionName The name of the session to be annotated in the classic histogram
     * @param pattern     A regular expression pattern to filter out metric names for inclusion
     * @param prefix      The name prefix to add to the classic histograms so that they fit into the existing metrics namespace
     * @param interval    How frequently to update the histogram
     */
    public static void addClassicHistos(String sessionName, String pattern, String prefix, String interval) {
        Pattern compiledPattern = Pattern.compile(pattern);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() -> new RuntimeException("Unable to parse interval spec:" + interval + "'"));

        ClassicHistoListener classicHistoListener =
            new ClassicHistoListener(get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        logger.debug("attaching histo listener " + classicHistoListener + " to the metrics registry.");
        get().addListener(classicHistoListener);

        ClassicTimerListener classicTimerListener =
            new ClassicTimerListener(get(), sessionName, prefix, compiledPattern, interval, TimeUnit.NANOSECONDS);
        logger.debug("attaching timer listener " + classicTimerListener + " to the metrics registry.");
        get().addListener(classicTimerListener);
    }

    /**
     * This should be called at the end of a process, so that open intervals can be finished, logs closed properly,
     * etc.
     *
     * @param showChart whether to chart metrics on console
     */
    public static void closeMetrics(boolean showChart) {
        logger.trace("Closing all registered metrics closable objects.");
        for (MetricsCloseable metricsCloseable : metricsCloseables) {
            logger.trace("closing metrics closeable: " + metricsCloseable);
            metricsCloseable.closeMetrics();
            if (showChart) {
                metricsCloseable.chart();
            }
        }
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

    public static void removeActivityMetrics(ActivityDef activityDef) {
        get().getMetrics().keySet().stream().filter(s -> s.startsWith(activityDef.getAlias() + "."))
            .forEach(get()::remove);
    }

}

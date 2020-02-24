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

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.HistogramLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * HistoIntervalLogger runs a separate thread to snapshotAndWrite encoded histograms on a regular interval.
 * It listens to the metrics registry for any new metrics that match the pattern. Any metrics
 * which both match the pattern and which are {@link EncodableHistogram}s are written the configured
 * logfile at the configured interval.
 */
public class HistoIntervalLogger extends  CapabilityHook<HdrDeltaHistogramAttachment> implements Runnable, MetricsCloseable  {
    private final static Logger logger = LoggerFactory.getLogger(HistoIntervalLogger.class);

    private final String sessionName;
    //    private final long intervalMillis;
    private long intervalLength;

    public File getLogfile() {
        return logfile;
    }

    private File logfile;
    private PrintStream logStream;
    private HistogramLogWriter writer;
    private Pattern pattern;

    private List<WriterTarget> targets = new CopyOnWriteArrayList<>();
    private PeriodicRunnable<HistoIntervalLogger> executor;
    private long lastRunTime;

    public HistoIntervalLogger(String sessionName, File file, Pattern pattern, long intervalLength) {
        this.sessionName = sessionName;
        this.logfile = file;
        this.pattern = pattern;
        this.intervalLength = intervalLength;
        startLogging();
    }

    public boolean matches(String metricName) {
        return pattern.matcher(metricName).matches();
    }

    /**
     * By convention, it is typical for the logging application
     * to use a comment to indicate the logging application at the head
     * of the log, followed by the log format version, a startLogging time,
     * and a legend (in that order).
     */
    public void startLogging() {
        try {
            logStream = new PrintStream(logfile);
            writer = new HistogramLogWriter(logStream);
            writer.outputComment("logging histograms for session " + sessionName);
            writer.outputLogFormatVersion();
            long currentTimeMillis = System.currentTimeMillis();
            writer.outputStartTime(currentTimeMillis);
            writer.setBaseTime(currentTimeMillis);
            writer.outputLegend();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while starting histogram log writer", e);
        }

        this.executor = new PeriodicRunnable<HistoIntervalLogger>(this.getInterval(),this);
        executor.startDaemonThread();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HistoLogger:" + this.pattern + ":" + this.logfile.getPath() + ":" + this.intervalLength);
        return sb.toString();
    }

    public long getInterval() {
        return intervalLength;
    }

    @Override
    public synchronized void onCapableAdded(String name, HdrDeltaHistogramAttachment chainedHistogram) {
        if (pattern.matcher(name).matches()) {
            this.targets.add(new WriterTarget(name,chainedHistogram.attachHdrDeltaHistogram()));
        }
    }

    @Override
    public synchronized void onCapableRemoved(String name, HdrDeltaHistogramAttachment capable) {
        this.targets.remove(new WriterTarget(name,null));
    }

    @Override
    protected Class<HdrDeltaHistogramAttachment> getCapabilityClass() {
        return HdrDeltaHistogramAttachment.class;
    }

    @Override
    public void run() {
        for (WriterTarget target : targets) {
            writer.outputIntervalHistogram(target.histoProvider.getNextHdrDeltaHistogram());
        }
        lastRunTime = System.currentTimeMillis();
    }

    @Override
    public void closeMetrics() {
        executor.close();
        long potentialWriteTime = System.currentTimeMillis();
        if (lastRunTime +1000 < potentialWriteTime) {
            logger.debug("Writing last partial histo log:" + this);
            run();
        } else {
            logger.debug("Not writing last partial histo log <1s:" + this);
        }
        logStream.close();
    }

    @Override
    public void chart() {
        HistoLogChartGenerator.generateChartFromHistoLog(this);
    }

    private static class WriterTarget implements Comparable<WriterTarget> {

        public String name;
        public HdrDeltaHistogramProvider histoProvider;

        public WriterTarget(String name, HdrDeltaHistogramProvider attach) {
            this.name = name;
            this.histoProvider = attach;
        }

        @Override
        public boolean equals(Object obj) {
            return name.equals(((WriterTarget)obj).name);
        }

        @Override
        public int compareTo(WriterTarget obj) {
            return name.compareTo(obj.name);
        }
    }

}

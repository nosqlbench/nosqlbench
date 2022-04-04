/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import org.HdrHistogram.Histogram;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoStatsCSVWriter {
    private final static Logger logger = LogManager.getLogger(HistoStatsCSVWriter.class);
    private final static String logFormatVersion = "1.0";
    private final File csvfile;
    //    FileWriter writer;
    PrintStream writer;
    private long baseTime;

    public HistoStatsCSVWriter(File csvFile) {
        this.csvfile = csvFile;
        writer = initFile(csvFile);
    }

    private PrintStream initFile(File logfile) {
        try {
            PrintStream writer = new PrintStream(logfile);
            return writer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public void outputComment(String comment) {
        writer.format(Locale.US, "#%s\n", comment);
    }

    public void outputLogFormatVersion() {
        writer.format(Locale.US, "#[Histogram log format version %s]\n", logFormatVersion);
    }

    public void outputStartTime(long startTime) {
        writer.format(
                Locale.US,
                "#[StartTime: %.3f (seconds since epoch), %s]\n",
                startTime / 1000.0,
                new Date(startTime)
        );
        writer.flush();
    }

    public void outputTimeUnit(TimeUnit timeUnit) {
        writer.format(
                Locale.US,
                "#[TimeUnit: %s]\n",
                timeUnit.toString()
        );
        writer.flush();
    }

    public void outputLegend() {
        writer.format(Locale.US, "#Tag,Interval_Start,Interval_Length,count,min,p25,p50,p75,p90,p95,p98,p99,p999,p9999,max\n");
    }

    public void writeInterval(Histogram h) {
        StringBuilder csvLine = new StringBuilder(1024);
        csvLine.append("Tag=").append(h.getTag()).append(",");
        Double start = ((double) h.getStartTimeStamp() - baseTime) / 1000.0D;
        Double end = ((double) h.getEndTimeStamp() - baseTime) / 1000.0D;
        String len = String.format(Locale.US, "%.3f", (end - start));
        csvLine.append(start);
        csvLine.append(",").append(len);
        csvLine.append(",").append(h.getTotalCount());
        csvLine.append(",").append(h.getMinValue());
        csvLine.append(",").append(h.getValueAtPercentile(25.00D));
        csvLine.append(",").append(h.getValueAtPercentile(50.00D));
        csvLine.append(",").append(h.getValueAtPercentile(75.00D));
        csvLine.append(",").append(h.getValueAtPercentile(90.00D));
        csvLine.append(",").append(h.getValueAtPercentile(95.00D));
        csvLine.append(",").append(h.getValueAtPercentile(98.00D));
        csvLine.append(",").append(h.getValueAtPercentile(99.00D));
        csvLine.append(",").append(h.getValueAtPercentile(99.90D));
        csvLine.append(",").append(h.getValueAtPercentile(99.99D));
        csvLine.append(",").append(h.getMaxValue());
        writer.println(csvLine);

    }
}

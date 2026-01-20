/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.reporters.ConsoleReporter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NBMetricsBuffer {

    private final static Logger logger = LogManager.getLogger(NBMetricsBuffer.class);
    private List<NBMetric> summaryMetrics = new ArrayList<>();
//    private PrintStream out = System.out;

//    public void setPrintStream(PrintStream out) {
//        this.out = out;
//    }

    public List<NBMetric> getSummaryMetrics() {
        return summaryMetrics;
    }

    public void setSummaryMetrics(List<NBMetric> summaryMetrics) {
        this.summaryMetrics = summaryMetrics;
    }

    public void addSummaryMetric(NBMetric metric) {
        this.summaryMetrics.add(metric);
    }

    public void clearSummaryMetrics() {
        this.summaryMetrics.clear();
    }

    public void printMetricSummary(NBComponent caller) {
        caller.getComponentProp(NBComponentProps.SUMMARY).ifPresent(
            summary -> {
                Config config = new Config(caller, summary);
                MetricsView snapshot = MetricsView.capture(summaryMetrics, 0L);
                for (PrintStream channel : config.fullChannels) {
                    try (ConsoleReporter summaryReporter = new NBCreators.ConsoleReporterBuilder(caller, channel).build()) {
                        summaryReporter.report(snapshot);
                    }
                }
                for (PrintStream channel : config.briefChannels) {
                    try (ConsoleReporter summaryReporter = new NBCreators.ConsoleReporterBuilder(caller, channel).build()) {
                        summaryReporter.reportCounts(snapshot);
                    }
                }

            }
        );
    }

    private static class Config {
        final List<PrintStream> fullChannels = new ArrayList<>();
        final List<PrintStream> briefChannels = new ArrayList<>();

        public Config(NBComponent caller, String reportSummaryTo) {
            final String[] destinationSpecs = reportSummaryTo.split(", *");
            long seconds = caller.getNanosSinceStart() / 1_000_000_000;

            for (final String spec : destinationSpecs)
                if ((null != spec) && !spec.isBlank()) {
                    final String[] split = spec.split(":", 2);
                    final String summaryTo = split[0];
                    final long summaryWhen = (2 == split.length) ? (Unit.secondsFor(split[1]).orElseThrow()) : 0;

                    PrintStream out = null;
                    switch (summaryTo.toLowerCase()) {
                        case "console":
                        case "stdout":
                            out = System.out;
                            break;
                        case "stderr":
                            out = System.err;
                            break;
                        default:
                            final String outName = summaryTo
                                .replaceAll("_SESSION_", caller.getLabels().valueOf("session"))
                                .replaceAll("_LOGS_", caller.getComponentProp("logsdir").orElseThrow());
                            try {
                                out = new PrintStream(new FileOutputStream(outName));
                                break;
                            } catch (final FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                    }

                    fullChannels.add(out);
//                    if (seconds > summaryWhen) fullChannels.add(out);
//                    else {
//                        logger.debug("Summarizing counting metrics only to {} with scenario duration of {}S (<{})", spec, summaryWhen, summaryWhen);
//                        briefChannels.add(out);
//                    }
                }

        }
    }

}

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
 *
 */

package io.nosqlbench.nb.api.components;

import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.reporters.ConsoleReporter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class NBMetricsBuffer {
    private List<NBMetric> summaryMetrics = new ArrayList<>();
    private PrintStream out = System.out;

    public void setPrintStream(PrintStream out) {
        this.out = out;
    }

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
        try(ConsoleReporter summaryReporter = new NBCreators.ConsoleReporterBuilder(caller, this.out).build()) {
            summaryReporter.reportOnce(summaryMetrics);
        }
    }
}

package io.nosqlbench.components;

import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.engine.metrics.reporters.ConsoleReporter;

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
        ConsoleReporter summaryReporter = new NBCreators.ConsoleReporterBuilder(caller, this.out).build();
        summaryReporter.reportOnce(summaryMetrics);
    }
}

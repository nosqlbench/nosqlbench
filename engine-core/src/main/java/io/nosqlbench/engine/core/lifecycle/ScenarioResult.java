/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.engine.core.lifecycle;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.logging.Log4JMetricsReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScenarioResult {

    private final static Logger logger = LogManager.getLogger(ScenarioResult.class);
    private final long startedAt;
    private final long endedAt;

    private Exception exception;
    private final String iolog;

    public ScenarioResult(String iolog, long startedAt, long endedAt) {
        this.iolog = iolog;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public ScenarioResult(Exception e, long startedAt, long endedAt) {
        this.iolog = e.getMessage();
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.exception = e;
    }

    public void reportElapsedMillis() {
        logger.info("-- SCENARIO TOOK " + getElapsedMillis() + "ms --");
    }

    public String getSummaryReport() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(ps)
            .build();
        consoleReporter.report();

        ps.flush();
        String result = os.toString(StandardCharsets.UTF_8);
        return result;
    }

    public void reportToConsole() {
        String summaryReport = getSummaryReport();
        System.out.println(summaryReport);
    }


    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public void rethrowIfError() {
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw ((RuntimeException) exception);
            } else {
                throw new RuntimeException(exception);
            }
        }
    }

    public String getIOLog() {
        return this.iolog;
    }

    public long getElapsedMillis() {
        return endedAt - startedAt;
    }

    public void reportTo(PrintStream out) {
        out.println(getSummaryReport());
    }

    public void reportToLog() {
        logger.debug("-- BEGIN METRICS DETAIL --");
        Log4JMetricsReporter reporter = Log4JMetricsReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .withLoggingLevel(Log4JMetricsReporter.LoggingLevel.DEBUG)
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(logger)
            .build();
        reporter.report();
        logger.info("-- END METRICS DETAIL --");
    }
}

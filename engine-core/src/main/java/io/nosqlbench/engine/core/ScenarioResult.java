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

package io.nosqlbench.engine.core;

import com.codahale.metrics.MetricFilter;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.logging.Log4JMetricsReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScenarioResult {

    private final static Logger logger = LogManager.getLogger(ScenarioResult.class);

    private Exception exception;
    private final String iolog;

    public ScenarioResult(String iolog) {
        this.iolog = iolog;
    }

    public ScenarioResult(Exception e) {
        this.iolog = e.getMessage();
        this.exception = e;
    }

    public void reportToLog() {
        logger.info("-- BEGIN METRICS DETAIL --");
        Log4JMetricsReporter reporter = Log4JMetricsReporter.forRegistry(ActivityMetrics.getMetricRegistry())
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .outputTo(logger)
                .build();
        reporter.report();
        logger.info("-- END METRICS DETAIL --");

    }


    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public void rethrowIfError() {
        if (exception!=null) {
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
}

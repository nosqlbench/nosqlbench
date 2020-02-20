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

package io.nosqlbench.core;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Slf4jReporter;
import io.nosqlbench.metrics.ActivityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScenarioResult {

    private final static Logger logger = LoggerFactory.getLogger(ScenarioResult.class);

    private Exception exception;
    private String iolog;
    private String report;

    public ScenarioResult(String iolog) {
        this.iolog = iolog;
    }

    public ScenarioResult(Exception e) {
        this.iolog = e.getMessage();
        this.exception = e;
    }

    public void reportToLog() {
        logger.info("-- BEGIN METRICS DETAIL --");
        Slf4jReporter reporter = Slf4jReporter.forRegistry(ActivityMetrics.getMetricRegistry())
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .outputTo(logger)
                .build();
        reporter.report();
        logger.info("-- END METRICS DETAIL --");

//        if (this.exception!=null && !(this.exception instanceof UserException)) {
//            logger.error("Scenario error: " + this.exception.getMessage(),this.exception);
//        }

    }


    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public String getIOLog() {
        return this.iolog;
    }
}

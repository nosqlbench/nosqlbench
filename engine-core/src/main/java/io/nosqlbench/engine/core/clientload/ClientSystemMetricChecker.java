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

package io.nosqlbench.engine.core.clientload;

import com.codahale.metrics.Gauge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class ClientSystemMetricChecker {
    private final int pollIntervalSeconds;
    private final ScheduledExecutorService scheduler;
    private List<ClientMetric> clientMetrics;

    public ClientSystemMetricChecker(int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.clientMetrics = new ArrayList<>();
    }

    public void addMetricToCheck(String name, Gauge<Double> metric, Double threshold) {
        addRatioMetricToCheck(name, metric, null, threshold, false);
    }

    public void addRatioMetricToCheck(String name, Gauge<Double> numerator, Gauge<Double> denominator, Double threshold, boolean retainPrev) {
        /**
         * Some "meaningful" system metrics are derived via:
         * - taking a ratio of instantaneous values (e.g. MemUsed / MemTotal from /proc/meminfo)
         * - taking a ratio of deltas of aggregates values over a time window (e.g. CPU utilization from /proc/stat)
         *
         * This method serves to be able to allow checking those which can be derived as a ratio of two existing metrics.
         */
        clientMetrics.add(new ClientMetric(name, numerator, denominator, threshold, retainPrev));
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            checkMetrics();
        }, pollIntervalSeconds, pollIntervalSeconds, TimeUnit.SECONDS);
    }

    private void checkMetrics() {
        for (ClientMetric c: clientMetrics)
            c.check();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private class ClientMetric {
        private static final Logger logger = LogManager.getLogger(ClientMetric.class);
        private final String name;
        private final Gauge<Double> numerator;
        private final Gauge<Double> denominator;
        private final Double threshold;
        private final Boolean retainPrevValue;
        private Double prevNumeratorValue;
        private Double prevDenominatorValue;

        private ClientMetric(String name, Gauge<Double> gauge, Double threshold) {
            this(name, gauge, null, threshold, false);
        }

        private ClientMetric(String name, Gauge<Double> numerator, Gauge<Double> denominator, Double threshold, Boolean retainPrevValue) {
            this.name = name;
            this.numerator = numerator;
            this.denominator = denominator;
            this.threshold = threshold;
            this.retainPrevValue = retainPrevValue;
            this.prevNumeratorValue = null;
            this.prevDenominatorValue = null;
        }

        private Double extract(){
            Double numeratorVal = numerator.getValue();
            if (numeratorVal == null)
                return null;
            Double deltaNumeratorVal = numeratorVal;
            if (prevNumeratorValue != null)
                deltaNumeratorVal -= prevNumeratorValue;
            // the case that we are not extracting a ratio of values
            if (denominator == null) {
                if (retainPrevValue)
                    prevNumeratorValue = numeratorVal;
                return deltaNumeratorVal;
            }
            // at this point, we will be extracting a ratio of gauge value changes over a time interval
            Double denominatorVal = denominator.getValue();
            if (denominatorVal == null)
                return null;
            Double deltaDenominatorVal = denominatorVal;
            if (prevDenominatorValue != null)
                deltaDenominatorVal -= prevDenominatorValue;
            if (deltaDenominatorVal == 0.0)
                return null;
            Double percent = (deltaNumeratorVal / deltaDenominatorVal) * 100.0;
            if (retainPrevValue) {
                prevNumeratorValue = numeratorVal;
                prevDenominatorValue = denominatorVal;
            }
            return percent;
        }

        private void check() {
            Double extractedVal = extract();
            if (extractedVal != null && extractedVal > threshold)
                logger.warn(name + " value = " + extractedVal + " > threshold " + threshold);
        }
    }
}

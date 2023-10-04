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

package io.nosqlbench.engine.api.activityimpl.uniform;

import com.codahale.metrics.Gauge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

public class ClientSystemMetricChecker {
    private static final Logger logger = LogManager.getLogger(ClientSystemMetricChecker.class);
    private final int pollIntervalSeconds;
    private final ScheduledExecutorService scheduler;
    private final Map<String,Gauge<Double>> nameToNumerator;
    private final Map<String,Gauge<Double>> nameToDenominator;
    private final Map<String,Double> nameToThreshold;
    private final Map<String,Double> nameToPrevNumeratorValue;
    private final Map<String,Double> nameToPrevDenominatorValue;
    private final Map<String,Boolean> nameToRetainPrevValue;

    public ClientSystemMetricChecker(int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
        this.scheduler = Executors.newScheduledThreadPool(1);
        nameToNumerator = new HashMap<>();
        nameToDenominator = new HashMap<>();
        nameToThreshold = new HashMap<>();
        nameToPrevNumeratorValue = new HashMap<>();
        nameToPrevDenominatorValue = new HashMap<>();
        nameToRetainPrevValue = new HashMap<>();
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
        nameToNumerator.put(name, numerator);
        if (denominator != null)
            nameToDenominator.put(name, denominator);
        nameToThreshold.put(name, threshold);
        nameToRetainPrevValue.put(name, retainPrev);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            checkMetrics();
        }, pollIntervalSeconds, pollIntervalSeconds, TimeUnit.SECONDS);
    }

    private void checkMetrics() {
        for (Entry<String,Gauge<Double>> entry: nameToNumerator.entrySet()) {
            String name = entry.getKey();
            Gauge<Double> numerator = entry.getValue();
            Gauge<Double> denominator = nameToDenominator.get(name);
            Double threshold = nameToThreshold.get(name);
            Double numeratorVal = numerator.getValue();
            if (numeratorVal == null)
                continue;
            Double deltaNumeratorVal = numeratorVal - nameToPrevNumeratorValue.getOrDefault(name, 0.0);
            // the case that we are not checking a ratio of values
            if (denominator == null) {
                if (deltaNumeratorVal > threshold)
                    logger.warn(name + " value = " + deltaNumeratorVal + " > threshold " + threshold);
                if (nameToRetainPrevValue.get(name))
                    nameToPrevNumeratorValue.put(name, numeratorVal);
                continue;
            }
            // at this point, we are checking ratio of gauge value changes over a time interval
            Double denominatorValue = denominator.getValue();
            if (denominatorValue == null)
                continue;
            Double deltaDenominatorVal = denominatorValue - nameToPrevDenominatorValue.getOrDefault(name, 0.0);
            if (deltaDenominatorVal != 0.0) {
                Double percent = (deltaNumeratorVal / deltaDenominatorVal) * 100.0;
                if (percent > threshold)
                    logger.warn(name + " value = " + percent + " > threshold " + threshold);
            }
            // finally, save these currently recorded values if required
            if (nameToRetainPrevValue.get(name)) {
                nameToPrevNumeratorValue.put(name, numeratorVal);
                nameToPrevDenominatorValue.put(name, denominatorValue);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}

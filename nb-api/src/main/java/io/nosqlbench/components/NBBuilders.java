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
 */

package io.nosqlbench.components;

import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class NBBuilders {
    private final Logger logger = LogManager.getLogger(NBBuilders.class);
    private final NBBaseComponent base;

    public NBBuilders(NBBaseComponent base) {
        this.base = base;
    }

    public NBMetricTimer timer(String metricFamilyName, int hdrdigits) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricTimer timer = new NBMetricTimer(labels, new DeltaHdrHistogramReservoir(labels, hdrdigits));
        base.addMetric(timer);
        return timer;
    }

    public NBMetricCounter counter(String metricFamilyName) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricCounter counter = new NBMetricCounter(labels);
        base.addMetric(counter);
        return counter;
    }

    public NBFunctionGauge gauge(String metricFamilyName, Supplier<Double> valueSource) {
        NBFunctionGauge gauge = new NBFunctionGauge(base, valueSource, metricFamilyName);
        base.addMetric(gauge);
        return gauge;
    }

    public NBMetricHistogram histogram(String metricFamilyName, int hdrdigits) {
        NBLabels labels = base.getLabels().and("name", metricFamilyName);
        NBMetricHistogram histogram = new NBMetricHistogram(labels, new DeltaHdrHistogramReservoir(labels, hdrdigits));
        base.addMetric(histogram);
        return histogram;
    }

    public AttachedMetricsSummaryReporter summaryReporter(int seconds, String... labelspecs) {
        logger.debug("attaching summary reporter to " + base.description());
        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
        AttachedMetricsSummaryReporter reporter = new AttachedMetricsSummaryReporter(base, extraLabels, seconds);
        return reporter;
    }
    public AttachedMetricsPushReporter pushReporter(String targetUri, int seconds,String... labelspecs) {
        NBLabels extraLabels = NBLabels.forKV((Object[]) labelspecs);
        AttachedMetricsPushReporter reporter = new AttachedMetricsPushReporter(targetUri, base, seconds, extraLabels);
        return reporter;
    }



}

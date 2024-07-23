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

package io.nosqlbench.nb.api.engine.metrics;

import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.stats.StatBucket;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleConsumer;


/**
 * Create a discrete stat reservoir as a gauge.
 */
public class WindowSummaryGauge implements NBMetricGauge, DoubleConsumer {
    private final NBLabels labels;
    private final Stat stat;
    private final StatBucket stats;
    private final String description;
    private final MetricCategory[] categories;
    private final int window;

    @Override
    public String typeName() {
        return "gauge";
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public MetricCategory[] getCategories() {
        return this.categories;
    }

    public enum Stat {
        Min,
        Max,
        Average,
        Count,
        Sum
    }


    public WindowSummaryGauge(int window, NBLabels labels, Stat stat, String description,
                              MetricCategory... categories) {
        this.labels = labels;
        this.stat = stat;
        this.description = description;
        this.categories = categories;
        this.window = window;
        this.stats = new StatBucket(window);
    }

    public synchronized void accept(double value) {
        stats.apply(value);
    }

    @Override
public synchronized Double getValue() {
        return switch(stat) {
            case Min -> stats.getMin();
            case Max -> stats.getMax();
            case Average -> stats.getAverage();
            case Count -> (double) stats.getCount();
            case Sum -> stats.getSum();
        };
    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return this.labels.toString()+":"+this.stats.toString();
    }
}

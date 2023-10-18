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

package io.nosqlbench.api.engine.metrics;

import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleConsumer;


/**
 * Create a discrete stat reservoir as a gauge.
 */
public class DoubleSummaryGauge implements NBMetricGauge<Double>, DoubleConsumer {
    private final NBLabels labels;
    private final Stat stat;
    private final DoubleSummaryStatistics stats;

    public enum Stat {
        Min,
        Max,
        Average,
        Count,
        Sum
    }

    public DoubleSummaryGauge(NBLabels labels, Stat stat, DoubleSummaryStatistics stats) {
        this.labels = labels;
        this.stat = stat;
        this.stats = stats;
    }

    public DoubleSummaryGauge(NBLabels labels, Stat stat) {
        this.labels = labels;
        this.stat = stat;
        this.stats = new DoubleSummaryStatistics();
    }

    public synchronized void accept(double value) {
        stats.accept(value);
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

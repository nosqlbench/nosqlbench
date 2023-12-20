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

package io.nosqlbench.nb.api.engine.metrics.wrappers;

import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.DoubleSummaryGauge;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelevancyMeasures implements NBLabeledElement {

    private final NBComponent parent;
    private final NBLabels labels;
    private final List<RelevancyFunction> functions = new ArrayList<>();
    private final List<DoubleSummaryGauge> gauges = new ArrayList<>();

    public RelevancyMeasures(NBComponent parent) {
        this(parent,NBLabels.forKV());
    }

    public RelevancyMeasures(NBComponent parent, NBLabels labels) {
        this.parent = parent;
        this.labels = labels;
    }

    public RelevancyMeasures(NBComponent parent, Object... labels) {
        this.parent = parent;
        this.labels = NBLabels.forKV(labels);
    }
    public RelevancyMeasures(NBComponent parent, Map<String,String> labels) {
        this(parent,NBLabels.forMap(labels));
    }

    @Override
    public NBLabels getLabels() {
        return parent.getLabels().and(labels);
    }

    public RelevancyMeasures addFunction(RelevancyFunction... f) {
        for (RelevancyFunction function : f) {
            this.functions.add(function);
            function.prependLabels(this);
            DoubleSummaryGauge gauge = parent.create().summaryGauge(function.getUniqueName(), List.of("average"), MetricCategory.Accuracy, DoubleSummaryGauge.Stat.Average.toString());
            this.gauges.add(gauge);
        }
        return this;
    }

    public void accept(int[] relevant, int[] actual) {
        for (int i = 0; i < functions.size(); i++) {
            double metricValue = functions.get(i).apply(relevant, actual);
            gauges.get(i).accept(metricValue);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DoubleSummaryGauge gauge : gauges) {
            sb.append(gauge.toString()).append("\n");
        }
        return sb.toString();
    }
}

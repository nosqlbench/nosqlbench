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

package io.nosqlbench.api.engine.metrics.wrappers;

import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.engine.metrics.DoubleSummaryGauge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelevancyMeasures implements NBLabeledElement {

    private final NBLabeledElement parent;
    private final NBLabels labels;
    private final List<RelevancyFunction> functions = new ArrayList<>();
    private final List<DoubleSummaryGauge> gauges = new ArrayList<>();

    public RelevancyMeasures(NBLabeledElement parent) {
        this(parent,NBLabels.forKV());
    }

    public RelevancyMeasures(NBLabeledElement parent, NBLabels labels) {
        this.parent = parent;
        this.labels = labels;
    }

    public RelevancyMeasures(NBLabeledElement parent, Object... labels) {
        this.parent = parent;
        this.labels = NBLabels.forKV(labels);
    }
    public RelevancyMeasures(NBLabeledElement parent, Map<String,String> labels) {
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
            // TODO: metrics
            // DoubleSummaryGauge gauge = ActivityMetrics.summaryGauge(function, function.getUniqueName());
            // this.gauges.add(gauge);
        }
        return this;
    }

    public void accept(int[] relevant, int[] actual) {
        for (int i = 0; i < functions.size(); i++) {
            double metricValue = functions.get(i).apply(relevant, actual);
            gauges.get(i).accept(metricValue);
        }
    }

}

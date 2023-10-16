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

package io.nosqlbench.api.engine.metrics.instruments;

import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;

import java.util.Map;
import java.util.function.Supplier;

public class NBFunctionGauge implements NBMetricGauge<Double> {

    private final Supplier<Double> source;
    private final NBLabeledElement parent;
    private final NBLabels labels;

    public NBFunctionGauge(NBComponent parent, Supplier<Double> source, String metricFamilyName, Map<String,String> additionalLabels) {
        this.parent = parent;
        this.labels = NBLabels.forMap(additionalLabels).and("name",metricFamilyName);
        this.source = source;
    }
    public NBFunctionGauge(NBComponent parent, Supplier<Double> source, String metricFamilyName) {
        this(parent, source, metricFamilyName,Map.of());
    }
    @Override
    public Double getValue() {
        return source.get();
    }

    @Override
    public NBLabels getLabels() {
        return parent.getLabels().and(this.labels);
    }

    @Override
    public String toString() {
        return description();
    }
}



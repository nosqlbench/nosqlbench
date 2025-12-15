/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.instruments;

import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;

/**
 * Use this gauge type when you are setting the gauge value directly. It is merely a holder
 * for the measurement which is injected at the discretion of the scenario.
 */
public class NBVariableGauge implements NBMetricGauge {
    private double value;
    private final NBLabeledElement parent;
    private final NBLabels labels;
    private String description;
    private String unit;
    private MetricCategory[] categories;

    public NBVariableGauge(
        NBComponent parent,
        String metricFamilyName,
        double initialValue,
        NBLabels additionalLabels,
        String description,
        String unit,
        MetricCategory... categories
    ) {
        this.parent = parent;
        this.labels = additionalLabels.andPairs("name", metricFamilyName);
        this.value = initialValue;
        this.description = description;
        this.unit = unit;
        this.categories = categories;

    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String typeName() {
        return "gauge";
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getUnit() {
        return this.unit;
    }

    @Override
    public MetricCategory[] getCategories() {
        return this.categories;
    }
}

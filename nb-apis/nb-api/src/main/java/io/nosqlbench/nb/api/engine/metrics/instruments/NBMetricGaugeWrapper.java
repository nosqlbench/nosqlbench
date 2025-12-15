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

import com.codahale.metrics.Gauge;
import io.nosqlbench.nb.api.labels.NBLabels;

public class NBMetricGaugeWrapper implements NBMetricGauge, NBMetric {

    private final Gauge<Double> gauge;
    private final NBLabels labels;
    private final String description;
    private final String unit;
    private final MetricCategory[] categories;

    public NBMetricGaugeWrapper(NBLabels labels, Gauge<Double> gauge, String description, String unit, MetricCategory... categories) {
        this.gauge = gauge;
        if (gauge.getValue() instanceof Double d) {
        } else {
            throw new RuntimeException("NBMetricGauges only support Double values");
        }
        this.labels = labels;
        this.description = description;
        this.unit = unit;
        this.categories = categories;
    }

    @Override
    public Double getValue() {
        return gauge.getValue();
    }

    @Override
    public NBLabels getLabels() {
        return labels;
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

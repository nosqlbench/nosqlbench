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

package io.nosqlbench.nb.api.engine.metrics.instruments;

import com.codahale.metrics.Meter;
import io.nosqlbench.nb.api.labels.NBLabels;

public class NBMetricMeter extends Meter implements NBMetric {

    private final NBLabels labels;
    private final MetricCategory[] categories;
    private final String description;

    public NBMetricMeter(NBLabels labels, String description, MetricCategory... categories) {
        this.labels = labels;
        this.description = description;
        this.categories = categories;
    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }

    @Override
    public String typeName() {
        return "meter";
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public MetricCategory[] getCategories() {
        return this.categories;
    }
}

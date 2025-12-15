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

import com.codahale.metrics.Metric;
import io.nosqlbench.nb.api.labels.NBLabeledElement;

public interface NBMetric extends Metric, NBLabeledElement {
    default String getHandle() {
        return this.getLabels().linearizeAsMetrics();
    }
    String typeName();

    String getDescription();

    /**
     * Returns the unit of measurement for this metric.
     * Units should follow OpenMetrics conventions (e.g., "seconds", "bytes", "operations", "percent").
     *
     * @return the unit of measurement, or empty string if dimensionless
     */
    String getUnit();

    MetricCategory[] getCategories();
}

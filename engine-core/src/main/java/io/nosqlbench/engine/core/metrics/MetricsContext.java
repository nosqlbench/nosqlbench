/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.core.metrics;

import io.nosqlbench.api.engine.activityapi.core.MetricRegistryService;
import io.nosqlbench.api.engine.metrics.MetricsRegistry;
import io.nosqlbench.api.engine.metrics.NBMetricsRegistry;
import io.nosqlbench.nb.annotations.Service;

@Service(value = MetricRegistryService.class, selector = "metrics-context")
public class MetricsContext implements MetricRegistryService {

    private static MetricsContext instance;

    private final MetricReporters metricReporters = MetricReporters.getInstance();
    private final MetricsRegistry metrics = new NBMetricsRegistry();

    public static MetricsContext getInstance() {
        synchronized (MetricsContext.class) {
            if (instance == null) {
                instance = new MetricsContext();
            }
        }
        return instance;
    }

    public MetricReporters getReporters() {
        return metricReporters;
    }

    public MetricsRegistry getMetrics() {
        return metrics;
    }

    /**
     * Convenience method to unclutter code. This will be used everywhere.
     * @return The default metric context.
     */
    public static MetricsRegistry metrics() {
        return getInstance().getMetrics();
    }

    @Override
    public MetricsRegistry getMetricRegistry() {
        return getMetrics();
    }
}

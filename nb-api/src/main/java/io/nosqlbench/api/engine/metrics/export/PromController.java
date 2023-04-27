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
 *
 */

package io.nosqlbench.api.engine.metrics.export;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

import static java.util.Objects.requireNonNullElse;


public class PromController {

    private PromController() {
    }

    public static PrometheusMeterRegistry createRegistry() {
        return PromController.createRegistry(null);
    }

    public static PrometheusMeterRegistry createRegistry(PrometheusConfig config) {
        return new PrometheusMeterRegistry(requireNonNullElse(config, PrometheusConfig.DEFAULT));
    }

    // Used for default meter registry
    public static PrometheusMeterRegistry createRegistryWithCollector() {
        return createRegistryWithCollector(null, null, null);
    }

    // Used for customized meter registry
    public static PrometheusMeterRegistry createRegistryWithCollector(PrometheusConfig config,
                                                                      CollectorRegistry collector,
                                                                      Clock clock) {

        return new PrometheusMeterRegistry(requireNonNullElse(config, PrometheusConfig.DEFAULT),
                requireNonNullElse(collector, CollectorRegistry.defaultRegistry),
                requireNonNullElse(clock, Clock.SYSTEM));
    }
}
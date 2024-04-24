/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Meter;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionMeterMetrics {
    private final ConcurrentHashMap<String, Meter> meters = new ConcurrentHashMap<>();
    private final Meter allerrors;
    private final NBComponent parent;

    public ExceptionMeterMetrics(final NBComponent parent) {
        this.parent = parent;
        this.allerrors = parent.create().meter("errormeters_ALL", MetricCategory.Errors,
            "all errors, regardless of type type, for the parent " + parent.description()
        );
    }

    public void mark(final String name) {
        Meter c = this.meters.get(name);
        if (null == c) synchronized (this.meters) {
            c = this.meters.computeIfAbsent(
                name,
                k -> parent.create().meter(
                    "errormeters_" + name,
                    MetricCategory.Errors,
                    name + " errors for " + parent.description()
                )
            );
        }
        c.mark();
        this.allerrors.mark();
    }

    public List<Meter> getMeters() {
        return new ArrayList<>(this.meters.values());
    }
}

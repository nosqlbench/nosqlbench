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

import com.codahale.metrics.Counter;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionCountMetrics {
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final Counter allerrors;
    private final NBComponent parent;

    public ExceptionCountMetrics(final NBComponent parent) {
        this.parent = parent;
        this.allerrors=parent.create().counter(
            "errors_ALL",
            MetricCategory.Errors,
            "all errors, regardless of type type, for " + parent.description()
        );
    }

    public void count(final String name) {
        Counter c = this.counters.get(name);
        if (null == c) synchronized (this.counters) {
            c = this.counters.computeIfAbsent(
                name,
                k -> parent.create().counter(
                    "errors_" + name,
                    MetricCategory.Errors,
                    name + " errors for " + parent.description()
                )
            );
        }
        c.inc();
        this.allerrors.inc();
    }

    public List<Counter> getCounters() {
        return new ArrayList<>(this.counters.values());
    }
}

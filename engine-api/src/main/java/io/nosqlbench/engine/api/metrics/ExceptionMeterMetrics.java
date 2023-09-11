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
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionMeterMetrics {
    private final ConcurrentHashMap<String, Meter> meters = new ConcurrentHashMap<>();
    private final Meter allerrors;
    private final NBLabeledElement parentLabels;

    public ExceptionMeterMetrics(final NBLabeledElement parentLabels) {
        this.parentLabels = parentLabels;
        this.allerrors = ActivityMetrics.meter(parentLabels, "errormeters_ALL");
    }

    public void mark(final String name) {
        Meter c = this.meters.get(name);
        if (null == c) synchronized (this.meters) {
            c = this.meters.computeIfAbsent(
                name,
                k -> ActivityMetrics.meter(this.parentLabels, "errormeters_" + name)
            );
        }
        c.mark();
        this.allerrors.mark();
    }

    public List<Meter> getMeters() {
        return new ArrayList<>(this.meters.values());
    }
}

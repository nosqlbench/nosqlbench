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

import com.codahale.metrics.Timer;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionTimerMetrics {
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final Timer allerrors;
    private final ActivityConfig activityDef;
    private final NBComponent parentLabels;

    public ExceptionTimerMetrics(final NBComponent parent, final ActivityConfig config) {
        this.activityDef = config;
        this.parentLabels = parent;

        this.allerrors=parent.create().timer(
            "errortimers_ALL",
            4,
            MetricCategory.Errors,
            "exception timers for all error types"
        );
    }

    public void update(final String name, final long nanosDuration) {
        Timer timer = this.timers.get(name);
        if (null == timer) synchronized (this.timers) {
            timer = this.timers.computeIfAbsent(
                name, k -> parentLabels.create().timer(
                    "errortimers_" + name,
                    3,
                    MetricCategory.Errors,
                    "exception timers for specific error '" + name + "'"
                )
            );
        }
        timer.update(nanosDuration, TimeUnit.NANOSECONDS);
        this.allerrors.update(nanosDuration, TimeUnit.NANOSECONDS);
    }

    public List<Timer> getTimers() {
        return new ArrayList<>(this.timers.values());
    }
}

/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Meter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionMeterMetrics {
    private final ConcurrentHashMap<Class<? extends Throwable>, Meter> meters = new ConcurrentHashMap<>();
    private final ActivityDef activityDef;

    public ExceptionMeterMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public void mark(Throwable t) {
        Meter c = meters.get(t.getClass());
        if (c == null) {
            synchronized (meters) {
                c = meters.computeIfAbsent(
                    t.getClass(),
                    k -> ActivityMetrics.meter(activityDef, "exceptions." + t.getClass().getSimpleName())
                );
            }
        }
        c.mark();
    }

    public List<Meter> getMeters() {
        return new ArrayList<>(meters.values());
    }
}

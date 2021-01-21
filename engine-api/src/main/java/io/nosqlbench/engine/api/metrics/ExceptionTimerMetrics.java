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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionTimerMetrics {
    private final ConcurrentHashMap<Class<? extends Throwable>, Timer> timers = new ConcurrentHashMap<>();
    private final ActivityDef activityDef;

    public ExceptionTimerMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public void update(Throwable throwable, long nanosDuration) {
        Timer timer = timers.get(throwable.getClass());
        if (timer == null) {
            synchronized (timers) {
                timer = timers.computeIfAbsent(
                    throwable.getClass(),
                    k -> ActivityMetrics.timer(activityDef, "exceptions." + throwable.getClass().getSimpleName())
                );
            }
        }
        timer.update(nanosDuration, TimeUnit.NANOSECONDS);
    }

    public List<Timer> getTimers() {
        return new ArrayList<>(timers.values());
    }
}

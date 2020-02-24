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

import com.codahale.metrics.Counter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception metering in a uniform way.
 */
public class ExceptionCountMetrics {
    private final ConcurrentHashMap<Class<? extends Throwable>, Counter> counters = new ConcurrentHashMap<>();
    private ActivityDef activityDef;

    public ExceptionCountMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public void count(Throwable e) {
        Counter c = counters.get(e.getClass());
        if (c == null) {
            synchronized (counters) {
                c = counters.computeIfAbsent(
                        e.getClass(),
                        k -> ActivityMetrics.counter(activityDef, "errorcounts." + e.getClass().getSimpleName())
                );
            }
        }
        c.inc();
    }
}

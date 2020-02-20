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

package io.nosqlbench.metrics;

import com.codahale.metrics.Histogram;
import io.nosqlbench.activityimpl.ActivityDef;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception histograms in a uniform way.
 * To use this, you need to have a way to get a meaningful magnitude
 * from each type of error you want to track.
 */
public class ExceptionHistoMetrics {
    private final ConcurrentHashMap<Class<? extends Throwable>, Histogram> counters = new ConcurrentHashMap<>();
    private ActivityDef activityDef;

    public ExceptionHistoMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public void update(Throwable e, long magnitude) {
        Histogram h = counters.get(e.getClass());
        if (h == null) {
            synchronized (counters) {
                h = counters.computeIfAbsent(
                        e.getClass(),
                        k -> ActivityMetrics.histogram(activityDef, "errorhistos." + e.getClass().getSimpleName())
                );
            }
        }
        h.update(magnitude);
    }
}

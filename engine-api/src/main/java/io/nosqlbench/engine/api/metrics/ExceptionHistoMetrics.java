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

import com.codahale.metrics.Histogram;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this to provide exception histograms in a uniform way.
 * To use this, you need to have a way to get a meaningful magnitude
 * from each type of error you want to track.
 */
public class ExceptionHistoMetrics {
    private final ConcurrentHashMap<String, Histogram> histos = new ConcurrentHashMap<>();
    private final ActivityDef activityDef;

    public ExceptionHistoMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public void update(String name, long magnitude) {
        Histogram h = histos.get(name);
        if (h == null) {
            synchronized (histos) {
                h = histos.computeIfAbsent(
                    name,
                    k -> ActivityMetrics.histogram(activityDef, "errorhistos." + name)
                );
            }
        }
        h.update(magnitude);
    }


    public List<Histogram> getHistograms() {
        return new ArrayList<>(histos.values());
    }
}

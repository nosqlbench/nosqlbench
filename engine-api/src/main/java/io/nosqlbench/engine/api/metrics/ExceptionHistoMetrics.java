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

import com.codahale.metrics.Histogram;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.components.NBComponent;

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
    private final Histogram allerrors;
    private final NBComponent parent;
    private final ActivityDef activityDef;

    public ExceptionHistoMetrics(final NBComponent parent, final ActivityDef activityDef) {
        this.parent = parent;
        this.activityDef = activityDef;
        this.allerrors = parent.create().histogram( "errorhistos_ALL", activityDef.getParams().getOptionalInteger("hdr_digits").orElse(4));
    }

    public void update(final String name, final long magnitude) {
        Histogram h = this.histos.get(name);
        if (null == h) synchronized (this.histos) {
            h = this.histos.computeIfAbsent(
                name,
                errName -> parent.create().histogram( "errorhistos_"+errName, this.activityDef.getParams().getOptionalInteger("hdr_digits").orElse(4))
            );
        }
        h.update(magnitude);
        this.allerrors.update(magnitude);
    }


    public List<Histogram> getHistograms() {
        return new ArrayList<>(this.histos.values());
    }
}

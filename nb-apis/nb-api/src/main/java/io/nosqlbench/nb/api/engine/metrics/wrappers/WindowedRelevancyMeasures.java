/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.wrappers;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.WindowSummaryGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.stats.StatBucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WindowedRelevancyMeasures implements NBLabeledElement {

    private final NBComponent parent;
    private final NBLabels labels;
    private final List<RelevancyFunction> functions = new ArrayList<>();
    private final List<WindowSummaryGauge> gauges = new ArrayList<>();
    private final int window;
    private int offset = 0;

    public WindowedRelevancyMeasures(NBComponent parent, int window) {
        this(parent, NBLabels.forKV(),window);
    }

    public WindowedRelevancyMeasures(NBComponent parent, NBLabels labels, int window) {
        this.parent = parent;
        this.labels = labels;
        this.window = window;
    }

    public WindowedRelevancyMeasures(NBComponent parent, Map<String, String> labels, int window) {
        this(parent, NBLabels.forMap(labels), window);
    }

    @Override
    public NBLabels getLabels() {
        return parent.getLabels().and(labels);
    }

    public WindowedRelevancyMeasures addFunction(RelevancyFunction... f) {
        for (RelevancyFunction function : f) {
            this.functions.add(function);
            function.prependLabels(this);
            WindowSummaryGauge gauge = parent.create().windowSummaryGauge(
                function.getUniqueName(),
                window,
                List.of("Average"),
                MetricCategory.Accuracy,
                WindowSummaryGauge.Stat.Average.toString()
            );
            this.gauges.add(gauge);
        }
        return this;
    }

    public void accept(int[] relevant, int[] actual) {
        offset++;
        if (offset >= window) {
            offset = 0;
        }

        for (int i = 0; i < functions.size(); i++) {
            double metricValue = functions.get(i).apply(relevant, actual);
            gauges.get(i).accept(metricValue);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (WindowSummaryGauge gauge : gauges) {
            sb.append(gauge.toString()).append("\n");
        }
        return sb.toString();
    }
}

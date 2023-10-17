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

package io.nosqlbench.components;

import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.engine.metrics.reporters.PromExpositionFormat;
import io.nosqlbench.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AttachedMetricsSummaryReporter extends PeriodicTaskComponent {
    private final static Logger logger = LogManager.getLogger(AttachedMetricsPushReporter.class);

    public AttachedMetricsSummaryReporter(NBComponent node, NBLabels extraLabels, long millis) {
        super(node, extraLabels, millis, true);
    }

    public void task() {
        final Clock nowclock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder(1024 * 1024); // 1M pre-allocated to reduce heap churn
        List<NBMetric> metrics = new ArrayList<>();
        Iterator<NBComponent> allMetrics = NBComponentTraversal.traverseBreadth(getParent());
        allMetrics.forEachRemaining(m -> metrics.addAll(m.findComponentMetrics("")));

        int total = 0;
        for (NBMetric metric : metrics) {
            sb = PromExpositionFormat.format(nowclock, sb, metric);
            total++;
        }
        AttachedMetricsSummaryReporter.logger.debug("formatted {} metrics in prom expo format", total);
        final String exposition = sb.toString();
        logger.info(() -> "prom exposition format:\n" + exposition);
    }
}

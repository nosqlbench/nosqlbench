/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.MetricsSnapshotScheduler;
import io.nosqlbench.nb.api.labels.NBLabels;

/**
 * Base class for reporters that consume {@link io.nosqlbench.nb.api.engine.metrics.view.MetricsView}
 * snapshots from the shared {@link MetricsSnapshotScheduler}. Reporters register themselves when
 * constructed and automatically unregister when closed.
 */
public abstract class MetricsSnapshotReporterBase extends NBBaseComponent
    implements MetricsSnapshotScheduler.MetricsSnapshotConsumer {

    private final MetricsSnapshotScheduler scheduler;
    private final long intervalMillis;

    protected MetricsSnapshotReporterBase(NBComponent parent,
                                          NBLabels extraLabels,
                                          long intervalMillis) {
        super(parent, extraLabels != null ? extraLabels : NBLabels.forKV());
        this.intervalMillis = intervalMillis;
        this.scheduler = MetricsSnapshotScheduler.register(parent, intervalMillis, this);
    }

    protected long getIntervalMillis() {
        return intervalMillis;
    }

    @Override
    protected void teardown() {
        scheduler.unregisterConsumer(this);
        super.teardown();
    }
}

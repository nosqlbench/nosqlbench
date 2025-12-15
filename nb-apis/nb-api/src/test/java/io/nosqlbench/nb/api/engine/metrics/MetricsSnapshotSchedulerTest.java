package io.nosqlbench.nb.api.engine.metrics;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricsSnapshotReporterBase;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetricsSnapshotSchedulerTest {

    private final NBComponent root = new NBBaseComponent(null);
    private final List<MetricsView> baseSnapshots = new ArrayList<>();
    private final List<MetricsView> coarseSnapshots = new ArrayList<>();
    private MetricsSnapshotScheduler scheduler;

    private NBLabels counterLabels() {
        return NBLabels.forKV("name", "counter_metric", "scenario", "scenario", "activity", "activity");
    }

    private MetricsView counterView(long value, long interval) {
        NBMetricCounter counter = new NBMetricCounter(counterLabels(), "counter", "operations", MetricCategory.Core);
        counter.inc(value);
        return MetricsView.capture(List.of(counter), interval);
    }

    @AfterEach
    public void cleanup() {
        if (scheduler != null) {
            scheduler.teardown();
            scheduler = null;
        }
        baseSnapshots.clear();
        coarseSnapshots.clear();
    }

    @Test
    public void testHierarchicalAggregation() {
        MetricsSnapshotScheduler.register(root, 100L, baseSnapshots::add);
        MetricsSnapshotScheduler.register(root, 300L, coarseSnapshots::add);
        scheduler = MetricsSnapshotScheduler.lookup(root);

        scheduler.injectSnapshotForTesting(counterView(1, 100L));
        scheduler.injectSnapshotForTesting(counterView(2, 100L));
        scheduler.injectSnapshotForTesting(counterView(3, 100L));

        assertThat(baseSnapshots).hasSize(3);
        assertThat(coarseSnapshots).hasSize(1);
        MetricsView aggregated = coarseSnapshots.getFirst();
        MetricsView.PointSample sample = (MetricsView.PointSample) aggregated.families().getFirst().samples().getFirst();
        assertThat(sample.value()).isEqualTo(6.0d);
        assertThat(aggregated.intervalMillis()).isEqualTo(300L);

        scheduler.injectSnapshotForTesting(counterView(4, 100L));
        scheduler.injectSnapshotForTesting(counterView(5, 100L));
        scheduler.injectSnapshotForTesting(counterView(6, 100L));

        assertThat(coarseSnapshots).hasSize(2);
        MetricsView second = coarseSnapshots.get(1);
        MetricsView.PointSample secondSample = (MetricsView.PointSample) second.families().getFirst().samples().getFirst();
        assertThat(secondSample.value()).isEqualTo(15.0d);
    }

    @Test
    public void testRejectsIncompatibleIntervals() {
        MetricsSnapshotScheduler.register(root, 100L, baseSnapshots::add);
        scheduler = MetricsSnapshotScheduler.lookup(root);
        assertThatThrownBy(() -> MetricsSnapshotScheduler.register(root, 250L, coarseSnapshots::add))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testRebasesWithSmallerInterval() {
        scheduler = MetricsSnapshotScheduler.register(root, 200L, coarseSnapshots::add);
        scheduler = MetricsSnapshotScheduler.register(root, 100L, baseSnapshots::add);

        assertThat(scheduler.getIntervalMillis()).isEqualTo(100L);

        scheduler.injectSnapshotForTesting(counterView(1, 100L));
        scheduler.injectSnapshotForTesting(counterView(2, 100L));

        assertThat(baseSnapshots).hasSize(2);
        assertThat(coarseSnapshots).hasSize(1);
        MetricsView coarse = coarseSnapshots.getFirst();
        MetricsView.PointSample sample = (MetricsView.PointSample) coarse.families().getFirst().samples().getFirst();
        assertThat(sample.value()).isEqualTo(3.0d);
        assertThat(coarse.intervalMillis()).isEqualTo(200L);
    }

    @Test
    public void testReporterRegistrationReceivesAggregatedCadences() {
        RecordingReporter fineReporter = new RecordingReporter(root, 100L);
        RecordingReporter coarseReporter = new RecordingReporter(root, 300L);
        scheduler = MetricsSnapshotScheduler.lookup(root);

        try {
            scheduler.injectSnapshotForTesting(counterView(1, 100L));
            scheduler.injectSnapshotForTesting(counterView(2, 100L));
            scheduler.injectSnapshotForTesting(counterView(3, 100L));

            assertThat(fineReporter.snapshots()).hasSize(3);
            assertThat(coarseReporter.snapshots()).hasSize(1);
            MetricsView coarseFirst = coarseReporter.snapshots().get(0);
            MetricsView.PointSample coarseSample = (MetricsView.PointSample) coarseFirst
                .families().getFirst().samples().getFirst();
            assertThat(coarseSample.value()).isEqualTo(6.0d);
            assertThat(coarseFirst.intervalMillis()).isEqualTo(300L);

            scheduler.injectSnapshotForTesting(counterView(4, 100L));
            scheduler.injectSnapshotForTesting(counterView(5, 100L));
            scheduler.injectSnapshotForTesting(counterView(6, 100L));

            assertThat(coarseReporter.snapshots()).hasSize(2);
            MetricsView coarseSecond = coarseReporter.snapshots().get(1);
            MetricsView.PointSample coarseSecondSample = (MetricsView.PointSample) coarseSecond
                .families().getFirst().samples().getFirst();
            assertThat(coarseSecondSample.value()).isEqualTo(15.0d);
        } finally {
            fineReporter.close();
            coarseReporter.close();
        }
    }

    private static final class RecordingReporter extends MetricsSnapshotReporterBase {
        private final List<MetricsView> snapshots = new ArrayList<>();

        private RecordingReporter(NBComponent parent, long intervalMillis) {
            super(parent, NBLabels.forKV("reporter", "recording", "interval", Long.toString(intervalMillis)), intervalMillis);
        }

        @Override
        public void onMetricsSnapshot(MetricsView view) {
            snapshots.add(view);
        }

        public List<MetricsView> snapshots() {
            return snapshots;
        }
    }
}

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
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricsSnapshotReporterBase;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
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
    private final List<MetricsView> mediumSnapshots = new ArrayList<>();
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
        mediumSnapshots.clear();
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
    public void testParallelCadenceAggregation() {
        MetricsSnapshotScheduler.register(root, 100L, baseSnapshots::add);
        MetricsSnapshotScheduler.register(root, 200L, mediumSnapshots::add);
        MetricsSnapshotScheduler.register(root, 300L, coarseSnapshots::add);
        scheduler = MetricsSnapshotScheduler.lookup(root);

        scheduler.injectSnapshotForTesting(counterView(1, 100L));
        scheduler.injectSnapshotForTesting(counterView(2, 100L));
        scheduler.injectSnapshotForTesting(counterView(3, 100L));
        scheduler.injectSnapshotForTesting(counterView(4, 100L));
        scheduler.injectSnapshotForTesting(counterView(5, 100L));
        scheduler.injectSnapshotForTesting(counterView(6, 100L));

        assertThat(baseSnapshots).hasSize(6);
        assertThat(mediumSnapshots).hasSize(3);
        assertThat(coarseSnapshots).hasSize(2);

        MetricsView mediumFirst = mediumSnapshots.getFirst();
        MetricsView.PointSample mediumFirstSample = (MetricsView.PointSample) mediumFirst
            .families().getFirst().samples().getFirst();
        assertThat(mediumFirst.intervalMillis()).isEqualTo(200L);
        assertThat(mediumFirstSample.value()).isEqualTo(3.0d);

        MetricsView mediumSecond = mediumSnapshots.get(1);
        MetricsView.PointSample mediumSecondSample = (MetricsView.PointSample) mediumSecond
            .families().getFirst().samples().getFirst();
        assertThat(mediumSecondSample.value()).isEqualTo(7.0d);

        MetricsView coarseFirst = coarseSnapshots.getFirst();
        MetricsView.PointSample coarseFirstSample = (MetricsView.PointSample) coarseFirst
            .families().getFirst().samples().getFirst();
        assertThat(coarseFirst.intervalMillis()).isEqualTo(300L);
        assertThat(coarseFirstSample.value()).isEqualTo(6.0d);

        MetricsView coarseSecond = coarseSnapshots.get(1);
        MetricsView.PointSample coarseSecondSample = (MetricsView.PointSample) coarseSecond
            .families().getFirst().samples().getFirst();
        assertThat(coarseSecondSample.value()).isEqualTo(15.0d);
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

    @Test
    public void testMultipleHdrConsumersSeeIdenticalPayload() {
        RecordingHdrConsumer consumer1 = new RecordingHdrConsumer();
        RecordingHdrConsumer consumer2 = new RecordingHdrConsumer();
        MetricsSnapshotScheduler.register(root, 100L, consumer1);
        MetricsSnapshotScheduler.register(root, 100L, consumer2);
        scheduler = MetricsSnapshotScheduler.lookup(root);

        NBLabels labels = NBLabels.forKV("name", "hist_metric", "scenario", "scenario", "activity", "activity");
        DeltaHdrHistogramReservoir reservoir = new DeltaHdrHistogramReservoir(labels, 3);
        NBMetricHistogram histogram = new NBMetricHistogram(labels, reservoir, "hist", "nanoseconds", MetricCategory.Core);
        histogram.update(10);
        histogram.update(20);

        scheduler.injectSnapshotForTesting(MetricsView.capture(List.of(histogram), 100L, true));

        assertThat(consumer1.snapshots).hasSize(1);
        assertThat(consumer2.snapshots).hasSize(1);
        assertThat(consumer1.snapshots.getFirst()).isSameAs(consumer2.snapshots.getFirst());

        MetricsView.SummarySample sample = (MetricsView.SummarySample) consumer1.snapshots.getFirst()
            .families().getFirst().samples().getFirst();
        EncodableHistogram payload = sample.snapshot().asEncodableHistogram().orElseThrow();
        assertThat(payload).isInstanceOf(Histogram.class);
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

    private static final class RecordingHdrConsumer implements MetricsSnapshotScheduler.MetricsSnapshotConsumer {
        private final List<MetricsView> snapshots = new ArrayList<>();

        @Override
        public void onMetricsSnapshot(MetricsView view) {
            snapshots.add(view);
        }

        @Override
        public boolean requiresHdrPayload() {
            return true;
        }
    }
}

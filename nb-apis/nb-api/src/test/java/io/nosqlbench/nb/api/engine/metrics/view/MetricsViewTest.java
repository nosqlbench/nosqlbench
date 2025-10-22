package io.nosqlbench.nb.api.engine.metrics.view;

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


import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGaugeWrapper;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsViewTest {

    private NBLabels labels(String name) {
        return NBLabels.forKV("name", name, "scenario", "scenario", "activity", "activity");
    }

    @Test
    public void testCombineCountersAggregatesValuesAndIntervals() {
        NBMetricCounter counter1 = new NBMetricCounter(labels("counter_metric"), "counter", MetricCategory.Core);
        counter1.inc(5);
        MetricsView view1 = MetricsView.capture(List.of(counter1), 1_000L);

        NBMetricCounter counter2 = new NBMetricCounter(labels("counter_metric"), "counter", MetricCategory.Core);
        counter2.inc(7);
        MetricsView view2 = MetricsView.capture(List.of(counter2), 2_000L);

        MetricsView combined = MetricsView.combine(List.of(view1, view2));

        assertThat(combined.intervalMillis()).isEqualTo(3_000L);
        assertThat(combined.capturedAt().toEpochMilli() - combined.windowStart().toEpochMilli())
            .isEqualTo(combined.intervalMillis());
        MetricsView.MetricFamily family = combined.families().getFirst();
        MetricsView.PointSample sample = (MetricsView.PointSample) family.samples().getFirst();
        assertThat(sample.value()).isEqualTo(12.0d);
    }

    @Test
    public void testCombineGaugesUsesWeightedAverage() {
        NBMetricGaugeWrapper gauge1 = new NBMetricGaugeWrapper(labels("gauge_metric"), () -> 10.0d, "gauge", MetricCategory.Core);
        NBMetricGaugeWrapper gauge2 = new NBMetricGaugeWrapper(labels("gauge_metric"), () -> 20.0d, "gauge", MetricCategory.Core);

        MetricsView view1 = MetricsView.capture(List.of(gauge1), 1_000L);
        MetricsView view2 = MetricsView.capture(List.of(gauge2), 2_000L);

        MetricsView combined = MetricsView.combine(List.of(view1, view2));
        MetricsView.PointSample sample = (MetricsView.PointSample) combined.families().getFirst().samples().getFirst();

        double expected = ((10.0d * 1_000d) + (20.0d * 2_000d)) / 3_000d;
        assertThat(sample.value()).isEqualTo(expected);
    }

    @Test
    public void testCombineSummariesAggregatesStatistics() {
        DeltaHdrHistogramReservoir reservoir1 = new DeltaHdrHistogramReservoir(labels("hist_metric"), 3);
        NBMetricHistogram histogram1 = new NBMetricHistogram(labels("hist_metric"), reservoir1, "hist", MetricCategory.Core);
        histogram1.update(10);
        histogram1.update(20);
        histogram1.update(30);
        MetricsView view1 = MetricsView.capture(List.of(histogram1), 1_000L);

        DeltaHdrHistogramReservoir reservoir2 = new DeltaHdrHistogramReservoir(labels("hist_metric"), 3);
        NBMetricHistogram histogram2 = new NBMetricHistogram(labels("hist_metric"), reservoir2, "hist", MetricCategory.Core);
        histogram2.update(40);
        histogram2.update(50);
        MetricsView view2 = MetricsView.capture(List.of(histogram2), 1_000L);

        MetricsView combined = MetricsView.combine(List.of(view1, view2));
        MetricsView.SummarySample sample = (MetricsView.SummarySample) combined.families().getFirst().samples().getFirst();

        assertThat(sample.statistics().count()).isEqualTo(5L);
        assertThat(sample.statistics().min()).isEqualTo(10.0d);
        assertThat(sample.statistics().max()).isEqualTo(50.0d);
        double expectedSum = view1.families().getFirst().samples().stream()
            .map(s -> (MetricsView.SummarySample) s)
            .mapToDouble(MetricsView.SummarySample::sum)
            .sum() + view2.families().getFirst().samples().stream()
            .map(s -> (MetricsView.SummarySample) s)
            .mapToDouble(MetricsView.SummarySample::sum)
            .sum();
        assertThat(sample.sum()).isEqualTo(expectedSum);
    }

    @Test
    public void testWindowStartMatchesInterval() {
        NBMetricCounter counter = new NBMetricCounter(labels("counter_metric_single"), "counter", MetricCategory.Core);
        counter.inc(3);
        MetricsView view = MetricsView.capture(List.of(counter), 1_500L);

        assertThat(view.capturedAt().toEpochMilli() - view.windowStart().toEpochMilli())
            .isEqualTo(1_500L);
    }

    @Test
    public void testCombineMetersAggregatesCountsAndRates() {
        NBLabels labels = labels("meter_metric");
        MetricsView.MeterSample sample1 = new MetricsView.MeterSample(
            "meter_metric",
            labels,
            10L,
            1.0d,
            2.0d,
            3.0d,
            4.0d
        );
        MetricsView.MetricFamily family1 = new MetricsView.MetricFamily(
            "meter_metric",
            "meter_metric",
            MetricsView.MetricType.GAUGE,
            "",
            "",
            List.of(),
            List.of(sample1)
        );
        MetricsView view1 = MetricsView.forTesting(
            Instant.now(),
            1_000L,
            List.of(family1)
        );

        MetricsView.MeterSample sample2 = new MetricsView.MeterSample(
            "meter_metric",
            labels,
            30L,
            5.0d,
            6.0d,
            7.0d,
            8.0d
        );
        MetricsView.MetricFamily family2 = new MetricsView.MetricFamily(
            "meter_metric",
            "meter_metric",
            MetricsView.MetricType.GAUGE,
            "",
            "",
            List.of(),
            List.of(sample2)
        );
        MetricsView view2 = MetricsView.forTesting(
            Instant.now().plusMillis(1_000L),
            2_000L,
            List.of(family2)
        );

        MetricsView combined = MetricsView.combine(List.of(view1, view2));

        assertThat(combined.intervalMillis()).isEqualTo(3_000L);
        MetricsView.MeterSample aggregated = (MetricsView.MeterSample) combined.families().getFirst().samples().getFirst();
        assertThat(aggregated.count()).isEqualTo(40L);
        assertThat(aggregated.meanRate()).isEqualTo(11.0d / 3.0d);
        assertThat(aggregated.oneMinuteRate()).isEqualTo(14.0d / 3.0d);
        assertThat(aggregated.fiveMinuteRate()).isEqualTo(17.0d / 3.0d);
        assertThat(aggregated.fifteenMinuteRate()).isEqualTo(20.0d / 3.0d);
    }
}

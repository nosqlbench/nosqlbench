package io.nosqlbench.nb.api.engine.metrics.reporters;

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
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Tag("accuracy")
@Tag("metrics")
public class CsvReporterHistogramFormattingTest {

    @Test
    public void testHistogramSampleDoesNotThrowOnFormatting(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels labels1 = NBLabels.forKV("name", "hist1", "scenario", "scenario", "activity", "activity");
            NBMetricHistogram histogram1 = new NBMetricHistogram(
                labels1,
                new DeltaHdrHistogramReservoir(labels1, 3),
                "histogram",
                "units",
                MetricCategory.Core
            );
            histogram1.update(10);
            histogram1.update(20);

            NBLabels labels2 = NBLabels.forKV("name", "hist2", "scenario", "scenario", "activity", "activity");
            NBMetricHistogram histogram2 = new NBMetricHistogram(
                labels2,
                new DeltaHdrHistogramReservoir(labels2, 3),
                "histogram",
                "units",
                MetricCategory.Core
            );
            histogram2.update(5);
            histogram2.update(15);

            MetricsView view = MetricsView.capture(List.of(histogram1, histogram2), 1_000L, false);

            assertThatCode(() -> reporter.onMetricsSnapshot(view)).doesNotThrowAnyException();

            Path csv1 = outDir.resolve("hist1.csv");
            Path csv2 = outDir.resolve("hist2.csv");
            assertThat(Files.exists(csv1)).isTrue();
            assertThat(Files.exists(csv2)).isTrue();

            List<String> lines1 = Files.readAllLines(csv1);
            assertThat(lines1).hasSize(2);
            assertThat(lines1.getFirst()).startsWith("t,count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999");
        } finally {
            root.close();
        }
    }
}

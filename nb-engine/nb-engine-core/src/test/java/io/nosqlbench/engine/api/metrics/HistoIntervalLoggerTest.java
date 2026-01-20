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

import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.HistoIntervalLogger;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.MetricsSnapshotScheduler;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("accuracy")
@Tag("statistics")
public class HistoIntervalLoggerTest {

    @Test
    public void testBasicLogger() throws IOException {
        File tempFile = File.createTempFile("testhistointlog", "hdr", new File("/tmp"));
        tempFile.deleteOnExit();

        NBBaseComponent root = new NBBaseComponent(null);
        HistoIntervalLogger hil = new HistoIntervalLogger(root, "loggertest", tempFile, Pattern.compile(".*"), 1000);
        MetricsSnapshotScheduler scheduler = MetricsSnapshotScheduler.lookup(root);

        final int significantDigits = 4;

        NBMetricHistogram NBHistogram = new NBMetricHistogram(
            NBLabels.forKV("name", "histo1"),
            new DeltaHdrHistogramReservoir(
                NBLabels.forKV("name", "histo1"),
                significantDigits
            ),
            "test basic logger",
            "nanoseconds",
            MetricCategory.Verification
        );

        NBHistogram.update(1L);
        scheduler.injectSnapshotForTesting(MetricsView.capture(List.of(NBHistogram), 1000L));
        delay(5);

        NBHistogram.update(1000000L);
        scheduler.injectSnapshotForTesting(MetricsView.capture(List.of(NBHistogram), 1000L));

        hil.closeMetrics();
        if (scheduler != null) {
            scheduler.teardown();
        }

        HistogramLogReader hlr = new HistogramLogReader(tempFile.getAbsolutePath());
        List<EncodableHistogram> histos = new ArrayList<>();
        EncodableHistogram histogram;
        while (true) {
            histogram = hlr.nextIntervalHistogram();
            if (null == histogram) {
                break;
            }
            histos.add(histogram);
        }

        assertThat(histos.size()).isEqualTo(2);
        assertThat(histos.get(0)).isInstanceOf(Histogram.class);
        assertThat(((Histogram) histos.get(0)).getNumberOfSignificantValueDigits()).isEqualTo(significantDigits);
    }

    private void delay(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException ignored) {
        }
    }

}

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

import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.engine.metrics.HistoIntervalLogger;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class HistoIntervalLoggerTest {

    @Test
    public void testBasicLogger() throws IOException {
//        File tempFile = new File("/tmp/testhistointlog.hdr");
        final File tempFile = File.createTempFile("testhistointlog", "hdr", new File("/tmp"));
        tempFile.deleteOnExit();

        final HistoIntervalLogger hil = new HistoIntervalLogger("loggertest", tempFile, Pattern.compile(".*"), 1000);

        final int significantDigits = 4;

        final NBMetricHistogram NBHistogram = new NBMetricHistogram(
                Map.of("name","histo1"), new DeltaHdrHistogramReservoir("histo1", significantDigits));

        hil.onHistogramAdded("histo1", NBHistogram);

        NBHistogram.update(1L);
        this.delay(1001);
        NBHistogram.update(1000000L);
        this.delay(1001);
        NBHistogram.update(1000L);
        hil.onHistogramRemoved("histo1");

        hil.closeMetrics();

        final HistogramLogReader hlr = new HistogramLogReader(tempFile.getAbsolutePath());
        final List<EncodableHistogram> histos = new ArrayList<>();
        EncodableHistogram histogram;
        while (true) {
            histogram = hlr.nextIntervalHistogram();
            if (null == histogram) break;
            histos.add(histogram);
        }

        assertThat(histos.size()).isEqualTo(2);
        assertThat(histos.get(0)).isInstanceOf(Histogram.class);
        assertThat(((Histogram)histos.get(0)).getNumberOfSignificantValueDigits()).isEqualTo(significantDigits);
    }

    private void delay(final int i) {
        final long now = System.currentTimeMillis();
        final long target = now+i;
        while (System.currentTimeMillis()<target) try {
            Thread.sleep(target - System.currentTimeMillis());
        } catch (final InterruptedException ignored) {
        }
        System.out.println("delayed " + (System.currentTimeMillis() - now) + " millis");
    }

}

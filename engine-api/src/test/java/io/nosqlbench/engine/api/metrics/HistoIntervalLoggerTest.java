/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class HistoIntervalLoggerTest {

    @Test
    public void testBasicLogger() throws IOException {
//        File tempFile = new File("/tmp/testhistointlog.hdr");
        File tempFile = File.createTempFile("testhistointlog", "hdr", new File("/tmp"));
        tempFile.deleteOnExit();

        HistoIntervalLogger hil = new HistoIntervalLogger("loggertest", tempFile, Pattern.compile(".*"), 1000);

        final int significantDigits = 4;

        NicerHistogram nicerHistogram = new NicerHistogram(
                "histo1", new DeltaHdrHistogramReservoir("histo1", significantDigits));

        hil.onHistogramAdded("histo1",nicerHistogram);

        List<Long> moments = new ArrayList<>(100);
        moments.add(System.currentTimeMillis()); // 0
        nicerHistogram.update(1L);
        moments.add(System.currentTimeMillis()); // 1
        delay(1001);
        moments.add(System.currentTimeMillis()); // 2
        nicerHistogram.update(1000000L);
        moments.add(System.currentTimeMillis()); // 3
        delay(1001);
        moments.add(System.currentTimeMillis()); // 4
        nicerHistogram.update(1000L);
        moments.add(System.currentTimeMillis()); // 5
        hil.onHistogramRemoved("histo1");
        moments.add(System.currentTimeMillis()); // 6

        hil.closeMetrics();

        HistogramLogReader hlr = new HistogramLogReader(tempFile.getAbsolutePath());
        List<EncodableHistogram> histos = new ArrayList<>();
        EncodableHistogram histogram;
        while (true) {
            histogram = hlr.nextIntervalHistogram();
            if (histogram==null) {
                break;
            }
            histos.add(histogram);
        };

        assertThat(histos.size()).isEqualTo(2);
        assertThat(histos.get(0)).isInstanceOf(Histogram.class);
        assertThat(((Histogram)histos.get(0)).getNumberOfSignificantValueDigits()).isEqualTo(significantDigits);
    }

    private void delay(int i) {
        long now = System.currentTimeMillis();
        long target = now+i;
        while (System.currentTimeMillis()<target) {
            try {
                Thread.sleep(target-System.currentTimeMillis());
            } catch (InterruptedException ignored) {
            } ;
        }
        System.out.println("delayed " + (System.currentTimeMillis() - now) + " millis");
    }

}

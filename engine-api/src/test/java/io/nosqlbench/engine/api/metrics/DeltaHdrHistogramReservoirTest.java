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

import com.codahale.metrics.Snapshot;
import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;

public class DeltaHdrHistogramReservoirTest {

//    @Test
//    public void testStartAndEndTimes() throws IOException {
////        File tempFile = new File("/tmp/test.hdr");
//        File tempFile = File.createTempFile("loghisto", "hdr", new File("/tmp"));
//        tempFile.deleteOnExit();
//
//        HistoLoggerConfig hlc = new HistoLoggerConfig("test1session",tempFile, Pattern.compile(".*"));
//
//        long beforeFirstHistoCreated=System.currentTimeMillis();
//        DeltaHdrHistogramReservoir dhhr = new DeltaHdrHistogramReservoir("test1metric",
//                new Recorder(4));
//
//        dhhr.attachLogWriter(hlc.getLogWriter());
//
//        writeAndSnapshot(dhhr,1,new long[]{1,20,300,4000,50000});
//        writeAndSnapshot(dhhr,2,new long[]{60000,7000,800,90,1});
//        long afterLastLoggedValue=System.currentTimeMillis();
//
//        HistogramLogReader hlr = new HistogramLogReader(tempFile.getAbsolutePath());
//        double startTimeSec = hlr.getStartTimeSec();
//
//        EncodableHistogram histo1 = hlr.nextIntervalHistogram();
//        long i1start = histo1.getStartTimeStamp();
//        long i1end = histo1.getEndTimeStamp();
//
//        EncodableHistogram histo2 = hlr.nextIntervalHistogram();
//        long i2start = histo2.getStartTimeStamp();
//        long i2end = histo2.getEndTimeStamp();
//        assertThat(i1start).isGreaterThanOrEqualTo(beforeFirstHistoCreated);
//        assertThat(i1end).isGreaterThan(i1start);
//        assertThat(i2start).isGreaterThanOrEqualTo(i1end);
//        assertThat(i2end).isLessThanOrEqualTo(afterLastLoggedValue);
//    }

    private void writeAndSnapshot(DeltaHdrHistogramReservoir dhhr, int interDelay, long[] longs) {
        for (long aLong : longs) {
            dhhr.update(aLong);
            try {
                Thread.sleep(interDelay);
            } catch (InterruptedException ignored) {
            }
        }
        Snapshot snapshot = dhhr.getSnapshot();
    }

}

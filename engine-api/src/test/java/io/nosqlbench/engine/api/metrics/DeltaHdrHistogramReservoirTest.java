package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Snapshot;
import org.junit.jupiter.api.Test;

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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Snapshot;
import org.junit.Ignore;
import org.junit.Test;

public class TestHistoTypes {

    @Test
    @Ignore
    public void compareHistos() {
        Clock c = new Clock();

        // Use the defaults that you get with "Timer()"
        ExponentiallyDecayingReservoir expRes = new ExponentiallyDecayingReservoir(1028,0.015,c);
        DeltaHdrHistogramReservoir hdrRes = new DeltaHdrHistogramReservoir("dr",4);
        long max=100000000;

        for (long i = 0; i < max; i++) {
            expRes.update(i);
            hdrRes.update(i);
            if ((i%1000000)==0) {
                System.out.println(i);
            }
        }

        summary(0L,max, expRes.getSnapshot(), hdrRes.getSnapshot());
    }

    private void summary(long min, long max,Snapshot... snapshots) {
        for (int i = 0; i <=100; i++) {
            double pct = (double)i/100.0D;
            double expectedValue=pct*max;
            System.out.format("% 3d %%p is % 11d : ",(long)(pct*100),(long)expectedValue);
            for (Snapshot snapshot : snapshots) {
                System.out.format("% 10d ",(long)snapshot.getValue(pct));
            }
            System.out.print("\n");
        }
    }

    private static class Clock extends com.codahale.metrics.Clock {

        public volatile long nanos;

        @Override
        public long getTime() {
            return nanos/1000000;
        }

        @Override
        public long getTick() {
            return nanos;
        }
    }
}

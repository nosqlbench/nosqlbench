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

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Snapshot;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestHistoTypes {

    @Test
    @Disabled
    public void compareHistos() {
        final Clock c = new Clock();

        // Use the defaults that you get with "Timer()"
        final ExponentiallyDecayingReservoir expRes = new ExponentiallyDecayingReservoir(1028,0.015,c);
        final DeltaHdrHistogramReservoir hdrRes = new DeltaHdrHistogramReservoir(NBLabels.forKV("name", "dr"),4);
        final long max=100000000;

        for (long i = 0; i < max; i++) {
            expRes.update(i);
            hdrRes.update(i);
            if (0 == (i % 1000000)) System.out.println(i);
        }

        this.summary(0L,max, expRes.getSnapshot(), hdrRes.getSnapshot());
    }

    private void summary(final long min, final long max, final Snapshot... snapshots) {
        for (int i = 0; 100 >= i; i++) {
            final double pct = i /100.0D;
            final double expectedValue=pct*max;
            System.out.format("% 3d %%p is % 11d : ",(long)(pct*100),(long)expectedValue);
            for (final Snapshot snapshot : snapshots) System.out.format("% 10d ", (long) snapshot.getValue(pct));
            System.out.print('\n');
        }
    }

    private static class Clock extends com.codahale.metrics.Clock {

        public volatile long nanos;

        @Override
        public long getTime() {
            return this.nanos /1000000;
        }

        @Override
        public long getTick() {
            return this.nanos;
        }
    }
}

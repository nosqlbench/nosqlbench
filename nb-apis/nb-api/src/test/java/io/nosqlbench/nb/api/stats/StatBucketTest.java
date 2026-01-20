/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.stats;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class StatBucketTest {
    @Test
    public void testStreamingMean() {
        var bucket = new StatBucket();
        bucket.apply(5.0d);
        assertThat(bucket.mean()).isCloseTo(5.0d, Offset.offset(0.001d));
        bucket.apply(10.0d);
        assertThat(bucket.mean()).isCloseTo(7.5d, Offset.offset(0.001d));
        bucket.apply(15.0d);
        assertThat(bucket.mean()).isCloseTo(10.0d, Offset.offset(0.001d));
        bucket.apply(20.0d);
        assertThat(bucket.mean()).isCloseTo(12.5d, Offset.offset(0.001d));
    }

    @Test
    public void testStreamingComputations() {
        double[] samples = new double[]{2, 4, 4, 4, 5, 5, 7, 9};

        var bucket = new StatBucket(8);
        for (int i = 0; i < samples.length * 10; i++) {
            bucket.apply(samples[i % samples.length]);
            if (i > 0 && (i % samples.length) == 0) {
                assertThat(bucket.mean()).isCloseTo(5, Offset.offset(0.001d));
                assertThat(bucket.stddev()).isCloseTo(2.0, Offset.offset(0.001d));
            }
        }
    }

    @Test
    public void testErrorAccumulation1() {
        var bucket = new StatBucket(11);
        for (long base = 1; base <10000000000000000L ; base*=10) {
            for (int i = 0; i< 10; i++) {
                long value = base+i;
                bucket.apply(value);
            }
            for (int i = 10; i < 20; i++) {
                long value = base+i;
                bucket.apply(value);
                double streamingMean = bucket.mean();
                assertThat(streamingMean).isCloseTo((double)(value-5), Offset.offset(0.03d));
            }
        }

    }

}

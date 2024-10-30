package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

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


import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliterator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
public class EnumeratedIntsTest {

    @Test
    public void EnumeratedIntsToInt() {
        EnumeratedInts ei = new EnumeratedInts("0 1 2 3 4");
        double counts[] = new double[5];
        Arrays.fill(counts,0.0d);
        int samples = 1000;
        for (int i = 0; i < samples; i++) {
            int v = ei.applyAsInt(i);
            assertThat(v).isGreaterThanOrEqualTo(0);
            assertThat(v).isLessThanOrEqualTo(4);
            counts[v] += 1.0d;
        }
        for (double count : counts) {
            assertThat(count/samples).isCloseTo((count/(double) samples), Offset.offset(0.01d));
        }
        StreamSupport.stream(spliterator(counts,0),false).forEach(System.out::println);
    }

    @Test
    public void EnumeratedIntsToLong() {
        io.nosqlbench.virtdata.library.curves4.discrete.long_int.EnumeratedInts ei =
                new io.nosqlbench.virtdata.library.curves4.discrete.long_int.EnumeratedInts ("0 1 2 3 4");
        double counts[] = new double[5];
        Arrays.fill(counts,0.0d);
        int samples = 1000;
        for (int i = 0; i < samples; i++) {
            long v = ei.applyAsInt(i);
            assertThat(v).isGreaterThanOrEqualTo(0);
            assertThat(v).isLessThanOrEqualTo(4);
            counts[(int) v] += 1.0d;
        }
        for (double count : counts) {
            assertThat(count/samples).isCloseTo((count/(double) samples), Offset.offset(0.01d));
        }
        StreamSupport.stream(spliterator(counts,0),false).forEach(System.out::println);
    }

    @Test
    public void EnumeratedLongToInt() {
        io.nosqlbench.virtdata.library.curves4.discrete.long_int.EnumeratedInts ei =
            new io.nosqlbench.virtdata.library.curves4.discrete.long_int.EnumeratedInts ("0 1 2 3 4");
        double counts[] = new double[5];
        Arrays.fill(counts,0.0d);
        int samples = 1000;
        for (int i = 0; i < samples; i++) {
            long v = ei.applyAsInt(i);
            assertThat(v).isGreaterThanOrEqualTo(0);
            assertThat(v).isLessThanOrEqualTo(4);
            counts[(int) v] += 1.0d;
        }
        for (double count : counts) {
            assertThat(count/samples).isCloseTo((count/(double) samples), Offset.offset(0.01d));
        }
        StreamSupport.stream(spliterator(counts,0),false).forEach(System.out::println);
    }

}

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

/*
 * Copyright (c) 2022 nosqlbench
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


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SequenceOfTest {

    @Test
    public void testSequenceSimpleToLong() {
        SequenceOf so = new SequenceOf(1L,"0 1 2 3 4 5 6 7 8 9");
        long[] results = new long[10];
        for (int i = 0; i < 10; i++) {
            results[i] = so.applyAsLong(i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(i,results[i]);
        }
    }

    @Test
    public void testSequenceSimpleToInt() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.SequenceOf so = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.SequenceOf(1,"0 1 2 3 4 5 6 7 8 9");
        long[] results = new long[10];
        for (int i = 0; i < 10; i++) {
            results[i] = so.applyAsInt(i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(i,results[i]);
        }
    }


    @Test
    public void testSequenceWeightedToLong() {
        SequenceOf so = new SequenceOf(1L,"0:6 1 2 3 4");
        int samples = 100;
        long[] results = new long[samples];
        for (int i = 0; i < samples; i++) {
            results[i]=so.applyAsLong(i);
        }
        assertThat(results[0]).isEqualTo(0);
        assertThat(results[1]).isEqualTo(0);
        assertThat(results[2]).isEqualTo(0);
        assertThat(results[3]).isEqualTo(0);
        assertThat(results[4]).isEqualTo(0);
        assertThat(results[5]).isEqualTo(0);
        assertThat(results[6]).isEqualTo(1);
        assertThat(results[7]).isEqualTo(2);
        assertThat(results[8]).isEqualTo(3);
        assertThat(results[9]).isEqualTo(4);
        assertThat(results[10]).isEqualTo(0);
    }

    @Test
    public void testSequenceWeightedToInt() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.SequenceOf so = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.SequenceOf(1,"0:6 1 2 3 4");
        int samples = 100;
        long[] results = new long[samples];
        for (int i = 0; i < samples; i++) {
            results[i]=so.applyAsInt(i);
        }
        assertThat(results[0]).isEqualTo(0);
        assertThat(results[1]).isEqualTo(0);
        assertThat(results[2]).isEqualTo(0);
        assertThat(results[3]).isEqualTo(0);
        assertThat(results[4]).isEqualTo(0);
        assertThat(results[5]).isEqualTo(0);
        assertThat(results[6]).isEqualTo(1);
        assertThat(results[7]).isEqualTo(2);
        assertThat(results[8]).isEqualTo(3);
        assertThat(results[9]).isEqualTo(4);
        assertThat(results[10]).isEqualTo(0);

    }


}

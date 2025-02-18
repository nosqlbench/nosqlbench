package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

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


import io.nosqlbench.virtdata.library.basics.shared.from_double.to_long.UnitHistribution;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnitHistributionTest {

    @Test
    public void testUniformSyntaxRequired() {
        assertThatThrownBy(() -> new UnitHistribution("1 2:2 3:3")).hasMessageContaining(
            "all elements must be");
    }

    @Test
    public void testBasicHistribution() {
        UnitHistribution h = new UnitHistribution("1:1 2:2 3:3");
        long[] counts = new long[10];
        int total=1000000;
        HashRange hr = new HashRange(0.0d, 1.0d);
        for (int i = 0; i < total; i++) {
            double hash = hr.applyAsDouble(i);
            long v = h.applyAsLong(hash);
            counts[(int)v]++;
        }
        assertThat((double) counts[0] / (double) total).isEqualTo(0.0d, Offset.offset(0.01));
        assertThat((double) counts[1] / (double) total).isEqualTo(0.16666666d, Offset.offset(0.01));
        assertThat((double) counts[2] / (double) total).isEqualTo(0.33333333d,
            Offset.offset(0.01));
        assertThat((double) counts[3] / (double) total).isEqualTo(0.5d, Offset.offset(0.01));
        System.out.println(Arrays.toString(counts));
    }

}

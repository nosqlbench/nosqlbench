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

package io.nosqlbench.virtdata.library.curves4.continuous;

import io.nosqlbench.virtdata.library.curves4.continuous.long_double.Enumerated;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumeratedTest {

    @Test
    public void testEnumerated() {
        Enumerated zt9 = new Enumerated("0 1 2 3 4 5 6 7 8 9", "map");
        assertThat(zt9.applyAsDouble(0)).isEqualTo(0.0d);

        double[] doubles = longs(0, Long.MAX_VALUE).mapToDouble(zt9).toArray();
        assertThat(doubles).containsExactly(0.0,9.0);

        Enumerated trapezoid = new Enumerated("0:0.0 2:1.0 8:1.0 10:0.0", "map");
        double[] traps = longs(0,1,2,3,4,5,6,7,8,9)
                .map(v -> (v*(Long.MAX_VALUE/9)))
                .mapToDouble(trapezoid)
                .toArray();
        assertThat(traps).containsExactly(2.0,2.0,2.0,2.0,2.0,8.0,8.0,8.0,8.0,8.0);
    }

    private static LongStream longs(long... v) {
        return Arrays.stream(v);
    }
}

/*
 * Copyright (c) 2023 nosqlbench
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


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CyclicDistanceTest {

    @Test
    public void testLongValues() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.CyclicDistance cyclicDistance =
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.CyclicDistance(100L);
        assertThat(cyclicDistance.applyAsLong(0)).isEqualTo(0);
        assertThat(cyclicDistance.applyAsLong(100)).isEqualTo(0);
        assertThat(cyclicDistance.applyAsLong(49)).isEqualTo(49);
        assertThat(cyclicDistance.applyAsLong(50)).isEqualTo(50);
        assertThat(cyclicDistance.applyAsLong(51)).isEqualTo(49);
    }

    @Test
    public void testDoubleValues() {
        io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.CyclicDistance cyclicDistance =
            new io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.CyclicDistance(100.0d);
        assertThat(cyclicDistance.applyAsDouble(0.0d)).isCloseTo(0.0d, Offset.offset(0.0001d));
        assertThat(cyclicDistance.applyAsDouble(100.0d)).isCloseTo(0.0d, Offset.offset(0.0001d));
        assertThat(cyclicDistance.applyAsDouble(49.0d)).isCloseTo(49.0d, Offset.offset(0.0001d));
        assertThat(cyclicDistance.applyAsDouble(50.0d)).isCloseTo(50.0d, Offset.offset(0.0001d));
        assertThat(cyclicDistance.applyAsDouble(51.0d)).isCloseTo(49.0d, Offset.offset(0.0001d));
    }

}

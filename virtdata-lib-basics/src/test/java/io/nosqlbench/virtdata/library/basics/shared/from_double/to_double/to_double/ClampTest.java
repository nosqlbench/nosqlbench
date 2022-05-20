package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.to_double;

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


import io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Clamp;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClampTest {

    @Test
    public void testDoubleUnaryClamp() {
        Clamp clamp = new Clamp(90.0d, 103.0d);
        assertThat(clamp.applyAsDouble(9.034D)).isCloseTo(90.0d, Offset.offset(0.0000001D));
        assertThat(clamp.applyAsDouble(90.34D)).isCloseTo(90.34d, Offset.offset(0.0000001D));
        assertThat(clamp.applyAsDouble(903.4D)).isCloseTo(103.0d, Offset.offset(0.0000001D));
    }

    @Test
    public void testIntUnaryClamp() {
        io.nosqlbench.virtdata.library.basics.shared.unary_int.Clamp clamp = new io.nosqlbench.virtdata.library.basics.shared.unary_int.Clamp(9, 13);
        assertThat(clamp.applyAsInt(8)).isEqualTo(9);
        assertThat(clamp.applyAsInt(9)).isEqualTo(9);
        assertThat(clamp.applyAsInt(10)).isEqualTo(10);
        assertThat(clamp.applyAsInt(100)).isEqualTo(13);
    }

    @Test
    public void testLongUnaryClamp() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Clamp clamp = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Clamp(9, 13);
        assertThat(clamp.applyAsLong(8L)).isEqualTo(9L);
        assertThat(clamp.applyAsLong(9L)).isEqualTo(9L);
        assertThat(clamp.applyAsLong(10L)).isEqualTo(10L);
        assertThat(clamp.applyAsLong(100L)).isEqualTo(13L);
    }

}

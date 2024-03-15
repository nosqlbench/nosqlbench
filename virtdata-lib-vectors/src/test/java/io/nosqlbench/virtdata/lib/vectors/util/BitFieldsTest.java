/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BitFieldsTest {

    @Test
    public void testZeros() {
        assertThat(BitFields.getLsbZeroBits(0)).isEqualTo(32);
        assertThat(BitFields.getLsbZeroBits(1)).isEqualTo(0);
        assertThat(BitFields.getLsbZeroBits(2)).isEqualTo(1);
        assertThat(BitFields.getLsbZeroBits(6)).isEqualTo(1);
        assertThat(BitFields.getLsbZeroBits(16)).isEqualTo(4);
        assertThat(BitFields.getLsbZeroBits(32)).isEqualTo(5);
        assertThat(BitFields.getLsbZeroBits(128)).isEqualTo(7);
        assertThat(BitFields.getLsbZeroBits(512)).isEqualTo(9);
        assertThat(BitFields.getLsbZeroBits(2049)).isEqualTo(0);
        assertThat(BitFields.getLsbZeroBits(8192)).isEqualTo(13);
        assertThat(BitFields.getLsbZeroBits(16384)).isEqualTo(14);
        assertThat(BitFields.getLsbZeroBits(4_000_000)).isEqualTo(8);
    }

    @Test
    public void testAligned() {
        assertThat(BitFields.alignReducedBits(new int[]{0,0})).isEqualTo(new int[]{0,0});
        assertThat(BitFields.alignReducedBits(new int[]{8,16})).isEqualTo(new int[]{1,2});
        assertThat(BitFields.alignReducedBits(new int[]{8,15})).isEqualTo(new int[]{8,15});
        assertThat(BitFields.alignReducedBits(new int[]{32768,16384})).isEqualTo(new int[]{2,1});
        assertThat(BitFields.alignReducedBits(new int[]{0,15})).isEqualTo(new int[]{0,15});
    }


}

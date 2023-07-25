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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharVectorsTest {


    /**
     * Verify radix mapping to aligned unit-interval step function. This shows the most conceptually
     * direct mapping to a vector.
     */
    @Test
    public void testBase10CharVectors() {
        DoubleVectors v10 = new DoubleVectors("0-9*12");

        assertThat(v10.getEncoding(0L)).isEqualTo("000000000000");
        assertThat(v10.apply(0L))
            .isEqualTo(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d});

        assertThat(v10.getEncoding(10L)).isEqualTo("000000000010");
        assertThat(v10.apply(10L))
            .isEqualTo(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.1d, 0.0d});

        assertThat(v10.getEncoding(1000000000L)).isEqualTo("001000000000");
        assertThat(v10.apply(1000000000L))
            .isEqualTo(new double[]{0.0d, 0.0d, 0.1d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d});

        assertThat(v10.getEncoding(999999999999L)).isEqualTo("999999999999");
        assertThat(v10.apply(999999999999L))
            .isEqualTo(new double[]{0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9});

    }

}

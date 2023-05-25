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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UniformVectorSteppedTest {


    @Test
    public void testUniformVectorSteppedIsEmpty() {
        UniformVectorSizedStepped f1 = new UniformVectorSizedStepped();
        List<Double> empty = f1.apply(1L);
        assertThat(empty).isEmpty();
    }

    @Test
    public void testUniformVectorSteppedHasDefaultFuncs() {
        UniformVectorSizedStepped f2 = new UniformVectorSizedStepped(2);
        List<Double> twoUniform = f2.apply(1L);
        assertThat(twoUniform).hasSize(2);
    }

    @Test
    public void testUniformVectorSteppedHasRanges() {
        UniformVectorSizedStepped f3 = new UniformVectorSizedStepped(4,3.0,5.0,7.0,9.0);
        for (int i = 0; i < 1000; i++) {
            List<Double> v4 = f3.apply(i);
            assertThat(v4.get(0)).isBetween(3.0d,5.0d);
            assertThat(v4.get(1)).isBetween(7.0d,9.0d);
            assertThat(v4.get(2)).isBetween(0.0d,1.0d);
            assertThat(v4.get(3)).isBetween(0.0d,1.0d);
        }
    }

}

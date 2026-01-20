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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.DoubleSummaryStatistics;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
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
        DoubleSummaryStatistics[] dssa = new DoubleSummaryStatistics[] {
            new DoubleSummaryStatistics(),
            new DoubleSummaryStatistics(),
            new DoubleSummaryStatistics(),
            new DoubleSummaryStatistics()
        };
        for (int i = 0; i < 1000; i++) {
            List<Double> v4 = f3.apply(i);
            for (int j = 0; j <= 3; j++) {
                dssa[j].accept(v4.get(j));
            }
        }
        assertThat(dssa[0].getMin()).isCloseTo(3.0d, Offset.offset(0.1d));
        assertThat(dssa[0].getMax()).isCloseTo(5.0d, Offset.offset(0.1d));
        assertThat(dssa[1].getMin()).isCloseTo(7.0d, Offset.offset(0.1d));
        assertThat(dssa[1].getMax()).isCloseTo(9.0d, Offset.offset(0.1d));
        assertThat(dssa[2].getMin()).isCloseTo(0.0d, Offset.offset(0.1d));
        assertThat(dssa[2].getMax()).isCloseTo(1.0d, Offset.offset(0.1d));
        assertThat(dssa[3].getMin()).isCloseTo(0.0d, Offset.offset(0.1d));
        assertThat(dssa[3].getMax()).isCloseTo(1.0d, Offset.offset(0.1d));
    }

}

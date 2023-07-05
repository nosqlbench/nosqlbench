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

package io.nosqlbench.datamappers.functions.geometry;

import com.datastax.oss.driver.api.core.data.CqlVector;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NormalizeCqlVectorTest {

    @Test
    public void normalizeCqlFloatVectorFloats() {
        CqlVector square = CqlVector.newInstance(1.0f, 1.0f);
        NormalizeCqlFloatVector nv = new NormalizeCqlFloatVector();
        CqlVector normalized = nv.apply(square);

        List sides = normalized.stream().toList();
        assertThat(sides.size()).isEqualTo(2);
        assertThat(sides.get(0)).isInstanceOf(Float.class);
        assertThat(sides.get(1)).isInstanceOf(Float.class);
        assertThat(((Float)sides.get(0)).doubleValue()).isCloseTo(0.707, Offset.offset(0.001d));
        assertThat(((Float)sides.get(1)).doubleValue()).isCloseTo(0.707, Offset.offset(0.001d));
    }

    @Test
    public void normalizeCqlVectorDoubles() {
        CqlVector square = CqlVector.newInstance(1.0d, 1.0d);
        NormalizeCqlFloatVector nv = new NormalizeCqlFloatVector();
        CqlVector normalized = nv.apply(square);

        List sides = normalized.stream().toList();
        assertThat(sides.size()).isEqualTo(2);
        assertThat(sides.get(0)).isInstanceOf(Double.class);
        assertThat(sides.get(1)).isInstanceOf(Double.class);
        assertThat(((Double)sides.get(0)).doubleValue()).isCloseTo(0.707, Offset.offset(0.001d));
        assertThat(((Double)sides.get(1)).doubleValue()).isCloseTo(0.707, Offset.offset(0.001d));
    }


}

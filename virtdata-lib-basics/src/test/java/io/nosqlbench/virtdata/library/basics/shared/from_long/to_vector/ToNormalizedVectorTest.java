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

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ToNormalizedVectorTest {

    @Test
    public void testNormalizeBasic() {
        NormalizeVector normalize = new NormalizeVector();
        List<Double> normalized = normalize.apply(List.of(1.0d));
        for (int i = 0; i < normalized.size(); i++) {
            assertThat(normalized.get(i)).isCloseTo(1.0d, Offset.offset(0.00001d));
        }
        normalized = normalize.apply(List.of(1.0d,1.0d));
        for (int i = 0; i < normalized.size(); i++) {
            assertThat(normalized.get(i)).isCloseTo(0.7071, Offset.offset(0.001d));
        }
        normalized = normalize.apply(List.of(1.0d,1.0d,1.0d));
        for (int i = 0; i < normalized.size(); i++) {
            assertThat(normalized.get(i)).isCloseTo(0.5773, Offset.offset(0.001d));
        }
        normalized = normalize.apply(List.of(100.0d,0.0d,0.0d,0.0d,0.0d,0.0d));
        assertThat(normalized.get(0)).isCloseTo(1.0d,Offset.offset(0.00001d));
        for (int i = 1; i < normalized.size(); i++) {
            assertThat(normalized.get(i)).isCloseTo(0.0, Offset.offset(0.0001d));
        }
    }

}

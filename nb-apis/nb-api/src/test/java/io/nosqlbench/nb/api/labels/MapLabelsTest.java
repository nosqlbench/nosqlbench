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

package io.nosqlbench.nb.api.labels;

import io.nosqlbench.nb.api.labels.MapLabels;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MapLabelsTest {

    @Test
    public void testLinearizeValues() {
        final MapLabels l1 = new MapLabels(Map.of("key_a", "value_a", "key_c", "value_c"));
        final String result = l1.linearizeValues('_', "key_a", "[key_b]", "key_c");
        assertThat(result).isEqualTo("value_a_value_c");
    }


    @Test
    public void testInvalidCharacters() {
        assertThatThrownBy(() -> new MapLabels(Map.of("a-b","c-d"))).isOfAnyClassIn(RuntimeException.class);
    }
}

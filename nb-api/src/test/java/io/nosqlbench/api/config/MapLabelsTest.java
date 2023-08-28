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

package io.nosqlbench.api.config;

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
    public void testInstances() {
        final MapLabels l1 = new MapLabels(Map.of("key_a", "value_a", "key_c", "value_c"),"key_c");
        NBLabels typesOnly = l1.onlyTypes();
        assertThat(typesOnly.linearizeValues()).isEqualTo("value_a");
    }

    @Test
    public void testInstanceCombination() {
        final MapLabels l1 = new MapLabels(Map.of("key_a", "value_a"),Map.of("key_c", "value_c"),"key_c");
        final MapLabels l2 = new MapLabels(Map.of("key_dog", "value_dog"),Map.of( "key_cat", "value_cat"),"key_dog");
        final MapLabels l3 = l1.and(l2);
        assertThat(l3.linearizeValues()).matches("value_a.value_c.value_dog.value_cat");

        assertThat(l3.onlyTypes().linearizeValues()).matches("value_a.value_cat");
        assertThat(l3.onlyInstances().linearizeValues()).matches("value_c.value_dog");
    }


    @Test
    public void testInvalidCharacters() {
        assertThatThrownBy(() -> new MapLabels(Map.of("a-b","c-d"))).isOfAnyClassIn(RuntimeException.class);
    }


}

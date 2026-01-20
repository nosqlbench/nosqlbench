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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.Instant;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class MapTest {

    @Test
    public void testMap() {
        Map mf = new Map((s) -> (int) s, (k) -> k, (v) -> v);
        java.util.Map<Object, Object> m1 = mf.apply(1L);
        assertThat(m1).containsOnlyKeys(1L);
        assertThat(m1).containsValues(1L);
    }

    @Test
    public void testStringMap() {
        StringMap sm = new StringMap((s) -> 2, (k) -> k, (v) -> v);
        java.util.Map<String, String> m2 = sm.apply(11L);
        assertThat(m2).containsOnlyKeys("11", "12");
        assertThat(m2).containsValues("11", "12");
    }

    @Test
    public void testMapTuple() {
        Map mf = new Map(s1 -> (int) s1, k2 -> (int) k2, s2 -> (int) s2, k2 -> (int) k2);
        java.util.Map<Object, Object> mt = mf.apply(37L);
        assertThat(mt).containsOnlyKeys(37, 38);
        assertThat(mt).containsValues(37, 38);
    }

    @Test
    public void testStringMapTuple() {
        StringMap mf =
            new StringMap(s1 -> (int) s1, k2 -> (int) k2, s2 -> (int) s2, k2 -> (int) k2);
        java.util.Map<String, String> mt = mf.apply(37L);
        assertThat(mt).containsOnlyKeys("37", "38");
        assertThat(mt).containsValues("37", "38");
    }

    @Test
    public void testLongFunctionsOnlyWithOddArity() {
        LongFunction<Object> sizeFunc = l -> (double) (l % 5);
        LongFunction<Object> keyfunc = String::valueOf;
        LongFunction<Object> valueFunc = String::valueOf;
        Map map = new Map(sizeFunc, keyfunc, valueFunc);
        java.util.Map<Object, Object> apply = map.apply(3L);
        assertThat(apply).isEqualTo(java.util.Map.of("3", "3", "4", "4", "5", "5"));
    }

    @Test
    public void testLongFunctionsOnlyWithInvalidSizer() {
        LongFunction<Object> sizeFunc = l -> Instant.ofEpochMilli(1L);
        LongFunction<Object> keyfunc = String::valueOf;
        LongFunction<Object> valueFunc = String::valueOf;
        assertThatThrownBy(() -> new Map(sizeFunc, keyfunc, valueFunc)).hasMessageContaining("An "
                                                                                             +
                                                                                             "even number of functions must be provided, unless the first one produces a numeric value");
    }

}

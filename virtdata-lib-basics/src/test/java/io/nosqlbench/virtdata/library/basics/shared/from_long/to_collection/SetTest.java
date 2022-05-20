package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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


import org.junit.jupiter.api.Test;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class SetTest {

    @Test
    public void testSet() {
        Set set = new Set((LongToIntFunction) s -> 3, (LongFunction<Object>) e -> e);
        java.util.Set<Object> s1 = set.apply(15L);
        assertThat(s1).containsOnly(15L,16L,17L);
    }

    @Test
    public void testStringSet() {
        StringSet set = new StringSet((LongToIntFunction) s -> 3, (LongToIntFunction) (e -> (int)e));
        java.util.Set<String> s1 = set.apply(15L);
        assertThat(s1).containsOnly("15","16","17");
    }
}

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

package io.nosqlbench.virtdata.core.bindings;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtDataFunctionResolverTest {

    @Test
    public void testArgSorting() {
        ArgsComparator comparator =
            new ArgsComparator(new Object[]{1, 2});
        ArrayList<Constructor<?>> ctors = new ArrayList<>();
        Constructor<?> long_boxed = LongBoxed.class.getConstructors()[0];
        Constructor<?> longs = Longs.class.getConstructors()[0];
        Constructor<?> mixed = Mixed.class.getConstructors()[0];
        Constructor<?> boxed = Boxed.class.getConstructors()[0];
        Constructor<?> primitives = Primitives.class.getConstructors()[0];

        ctors.add(long_boxed);
        ctors.add(longs);
        ctors.add(mixed);
        ctors.add(boxed);
        ctors.add(primitives);

        Collections.sort(ctors,comparator);
        assertThat(ctors).containsExactly(mixed, boxed, primitives,longs,long_boxed);
    }

    private static class Primitives {
        public Primitives(int a, int b) {
        }
    }

    private static class Boxed {
        public Boxed(Integer a, Integer b) {
        }
    }

    private static class Mixed {
        public Mixed(int a, Integer b) {
        }
    }

    private static class Longs {
        public Longs(long a, long b) {
        }
    }

    private static class LongBoxed {
        public LongBoxed(Long a, Long b) {}
    }
}

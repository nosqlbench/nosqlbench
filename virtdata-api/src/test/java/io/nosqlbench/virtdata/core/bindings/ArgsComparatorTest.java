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

import static org.assertj.core.api.Assertions.assertThat;

public class ArgsComparatorTest {


    static Constructor<?> ctor_longBoxed = LongBoxed.class.getConstructors()[0];
    static Constructor<?> ctor_longs = Longs.class.getConstructors()[0];
    static Constructor<?> ctor_mixed = Mixed.class.getConstructors()[0];
    static Constructor<?> ctor_boxed = Boxed.class.getConstructors()[0];
    static Constructor<?> ctor_primitives = Primitives.class.getConstructors()[0];


    @Test
    public void verifyRanks() {
        Object[] args = {1, 2};
        ArgsComparator comparator = new ArgsComparator(args);
        assertThat(comparator.matchRank(ctor_longBoxed,args)).isEqualTo(ArgsComparator.MATCHRANK.INCOMPATIBLE);
        assertThat(comparator.matchRank(ctor_longs,args)).isEqualTo(ArgsComparator.MATCHRANK.BOXED);
        assertThat(comparator.matchRank(ctor_mixed,args)).isEqualTo(ArgsComparator.MATCHRANK.DIRECT);
        assertThat(comparator.matchRank(ctor_boxed,args)).isEqualTo(ArgsComparator.MATCHRANK.DIRECT);
        assertThat(comparator.matchRank(ctor_primitives,args)).isEqualTo(ArgsComparator.MATCHRANK.DIRECT);
    }

    @Test
    public void testCtorSanity() {
        Object[] args = {1, 2};
        try {
            ctor_boxed.newInstance(args);
            ctor_longs.newInstance(args);
            ctor_mixed.newInstance(args);
            ctor_primitives.newInstance(args);
//            ctor_longBoxed.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

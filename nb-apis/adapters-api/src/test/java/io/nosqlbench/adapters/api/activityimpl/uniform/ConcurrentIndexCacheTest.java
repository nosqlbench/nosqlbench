package io.nosqlbench.adapters.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcurrentIndexCacheTest {

    @Test
    public void testBasicCache() {
        ConcurrentIndexCache<String> sc = new ConcurrentIndexCache<>("testing1",l -> String.valueOf(l));
        String s = sc.get(300);
        assertThat(s).isEqualTo("300");
    }

    @Test
    public void testCount() {
        ConcurrentIndexCache<String> sc = new ConcurrentIndexCache<>("testing2",l -> String.valueOf(l));
        for (int i = 0; i < 1000; i++) {
            String name = sc.get(i);
        }
        assertThat(sc.size()).isEqualTo(1000);
        for (int i = 999; i > 0; i-=2) {
            sc.remove(i);
        }
        assertThat(sc.size()).isEqualTo(500);
        assertThat(sc.remove(1001)).isNull();
    }

    @Test
    public void TestTraversal() {

        int[] indices = new int[1000];
        for (int i = 0; i < 1000; i++) {
            // generate an assortment of in-range indexes, but not all of them
            indices[i] = (int) (Math.abs(Math.sin((double)i)*1000));
        }
        int[] distinct = Arrays.stream(indices).sorted().distinct().toArray();

        ConcurrentIndexCache<String> sc = new ConcurrentIndexCache<>("testing3");
        for (int i : distinct) {
            sc.get(i,l -> String.valueOf(l));
        }

        Iterator<String> iter = sc.iterator();
        for (int i = 0; i < distinct.length; i++) {
            assertThat(iter.hasNext()).isTrue();
            String nextValue = iter.next();
            assertThat(nextValue.equals(String.valueOf(distinct[i])));
        }

        sc.clear();
        assertThat(sc.size()==0);
    }

    @Test
    public void testSafetyLimit() {
        ConcurrentIndexCache<String> sc = new ConcurrentIndexCache<>("testing4", String::valueOf, 1000);
        assertThat(sc.get(1000)).isNotNull();
        assertThat(sc.remove(11000)).isNull();
        assertThatThrownBy(() -> sc.get(1001)).hasMessageContaining("too high");
    }

}

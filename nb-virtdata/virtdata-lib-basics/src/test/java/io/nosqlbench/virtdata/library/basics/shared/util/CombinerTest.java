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

package io.nosqlbench.virtdata.library.basics.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CombinerTest {

    @Test
    public void testCharsetRangingBasic() {
        char[][] chars = Combiner.parseSpec("a-e");
        assertThat(chars).isEqualTo(new char[][]{{'a','b','c','d','e'}});
    }

    @Test
    public void testCharsetRangingRepeat() {
        char[][] chars = Combiner.parseSpec("a-c*3");
        assertThat(chars).isEqualTo(new char[][]{{'a','b','c'},{'a','b','c'},{'a','b','c'}});
    }

    @Test
    public void testInvertedIndex() {
        char[][] chars = Combiner.parseSpec("a-c*3");
        int[][] ints = Combiner.invertedIndexFor("a-c*3");
        assertThat(chars[0][0]).isEqualTo('a');
        assertThat(chars[1][1]).isEqualTo('b');
        assertThat(chars[2][2]).isEqualTo('c');
        assertThat(ints[0]['a']).isEqualTo(0);
        assertThat(ints[1]['b']).isEqualTo(1);
        assertThat(ints[2]['c']).isEqualTo(2);
    }

    @Test
    public void testBasicSequence() {
        Combiner<String> stringCombiner = new Combiner<>("0-9*10", String::valueOf, String.class);
        assertThat(stringCombiner.apply(123456789L)).isEqualTo(new String[]{"0","1","2","3","4","5","6","7","8","9"});
        long ordinal = stringCombiner.getOrdinal("0123456789");
        assertThat(stringCombiner.getEncoding(ordinal)).isEqualTo("0123456789");
        assertThat(ordinal).isEqualTo(123456789L);
        assertThat(stringCombiner.getIndexes(123456789L)).isEqualTo(new int[]{0,1,2,3,4,5,6,7,8,9});
        assertThat(stringCombiner.getOrdinal(new int[]{0,1,2,3,4,5,6,7,8,9})).isEqualTo(123456789L);
    }

    @Test
    public void testRangeFor() {
        assertThat(Combiner.rangeFor("3")).isEqualTo(new char[]{'3'});
        assertThat(Combiner.rangeFor("3-5")).isEqualTo(new char[]{'3','4','5'});
        assertThat(Combiner.rangeFor("345")).isEqualTo(new char[]{'3','4','5'});
        assertThat(Combiner.rangeFor("3-45")).isEqualTo(new char[]{'3','4','5'});
    }

    @Test
    public void testChains() {
        Combiner<String> combiner = new Combiner<>("ab*2", String::valueOf, String.class);
        long correctInput=3;
        int[] correctIndexes = new int[]{1,1};
        String correctEncoding = "bb";
        String[] correctValues = new String[]{"1","1"};

        assertThat(combiner.apply(correctInput)).isEqualTo(correctValues);
        assertThat(combiner.getArray(correctIndexes)).isEqualTo(correctValues);
        assertThat(combiner.getArray(correctEncoding)).isEqualTo(correctValues);

        assertThat(combiner.getIndexes(correctInput)).isEqualTo(correctIndexes);
        assertThat(combiner.getIndexes(correctEncoding)).isEqualTo(correctIndexes);

        assertThat(combiner.getOrdinal(correctIndexes)).isEqualTo(correctInput);
        assertThat(combiner.getOrdinal(correctEncoding)).isEqualTo(correctInput);

        assertThat(combiner.getEncoding(correctIndexes)).isEqualTo(correctEncoding);
        assertThat(combiner.getEncoding(correctInput)).isEqualTo(correctEncoding);

    }



}

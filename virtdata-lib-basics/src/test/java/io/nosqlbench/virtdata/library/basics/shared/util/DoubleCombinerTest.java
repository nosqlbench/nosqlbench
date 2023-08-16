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

public class DoubleCombinerTest {

    @Test
    public void testBasicSequence() {
        DoubleCombiner dc = new DoubleCombiner("0-9*10", l -> (double)l);
        assertThat(dc.apply(123456789L)).isEqualTo(new double[]{0.0d,1.0d,2.0d,3.0d,4.0d,5.0d,6.0d,7.0d,8.0d,9.0d});
        long ordinal = dc.getOrdinal("0123456789");
        assertThat(dc.getEncoding(ordinal)).isEqualTo("0123456789");
        assertThat(ordinal).isEqualTo(123456789L);
        assertThat(dc.getIndexes(123456789L)).isEqualTo(new int[]{0,1,2,3,4,5,6,7,8,9});
        assertThat(dc.getOrdinal(new int[]{0,1,2,3,4,5,6,7,8,9})).isEqualTo(123456789L);
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

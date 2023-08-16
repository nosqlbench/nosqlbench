/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.planning;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcatSequencerTest {

    public final static String a = "a";
    public final static String b = "b";
    public final static String c = "c";
    public final static String d = "d";
    public final static String e = "e";

    @Test
    public void testSeqIndexesByRatios() throws Exception {
        ConcatSequencer<String> concat = new ConcatSequencer<>();
        int[] ints = concat.seqIndexesByRatios(strings(a,b), ratios(2, 5));
        assertThat(ints).containsExactly(0,0,1,1,1,1,1);
    }

    @Test
    public void testSeqIndexWithZeroRatios() {
        ConcatSequencer<String> concat = new ConcatSequencer<>();
        int[] ints = concat.seqIndexesByRatios(strings(a,b), ratios(0,3));
        assertThat(ints).containsExactly(1,1,1);
    }

    private static List<String> strings(String... strings) {
        return Arrays.asList(strings);
    }
    private static List<Long> ratios(long... longs) {
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }
}

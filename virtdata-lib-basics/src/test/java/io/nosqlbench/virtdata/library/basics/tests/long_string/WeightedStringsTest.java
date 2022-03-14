/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.tests.long_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.WeightedStrings;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WeightedStringsTest {

    /**
     * A simple qualitative test to demonstrate expected values.
     */
    @Test
    public void testDigits() {
        WeightedStrings fiveAndOnes = new WeightedStrings("zero:0.1;one:0.1;two:0.1;three:0.1;four:0.1;fiveup:0.5");
        Map<String,Integer> hist = new HashMap<String,Integer>() {{
            put("zero",0);
            put("one",0);
            put("two",0);
            put("three",0);
            put("four",0);
            put("fiveup",0);
        }};
        for (long i = 0; i < 10000; i++) {
            String label = fiveAndOnes.apply(i);
            hist.put(label, hist.get(label)+1);
        }
        System.out.println(hist);
        assertThat(hist.get("zero")).isBetween(950,1050);
        assertThat(hist.get("one")).isBetween(950,1050);
        assertThat(hist.get("two")).isBetween(950,1050);
        assertThat(hist.get("three")).isBetween(950,1050);
        assertThat(hist.get("four")).isBetween(950,1050);
        assertThat(hist.get("fiveup")).isBetween(4950,5050);

    }

}

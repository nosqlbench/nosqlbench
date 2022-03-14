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

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.AlphaNumericString;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AlphaNumericStringTest {

    @Test
    public void testAlphaNumericStringZeroLength() {
        AlphaNumericString alphaNumeric = new AlphaNumericString(0);
        for (long cycle = 0; cycle < 5; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value).isEqualTo("");
        }
    }

    @Test
    public void testAlphaNumericStringBasic() {
        AlphaNumericString alphaNumeric = new AlphaNumericString(20);
        Set<String> seen = new HashSet<>();
        for (long cycle = 0; cycle < 5000; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value.length()).isEqualTo(20);
            assertThat(seen).doesNotContain(value);
            seen.add(value);
        }
        for (long cycle = 0; cycle < 5000; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value.length()).isEqualTo(20);
            assertThat(seen).contains(value);
        }
    }
}

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

package io.nosqlbench.virtdata.library.basics.shared.from_string;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchRegexTest {

    @Test
    public void testMatchRegex() {
        MatchRegex mr = new MatchRegex(
                "1","one",
                ".*2","two",
                "3(.+5.*)9", "$0",
                "4(.6..)9", "$1",
                "(75-(.*)-4)","$2",
                ".*(25|6to4).*", "$1",
                "([0-9]+)-([0-9]+)-([0-9]+)","$1 $2 $3"
        );
        assertThat(mr.apply("1")).isEqualTo("one");
        assertThat(mr.apply("this-is-2")).isEqualTo("two");
        assertThat(mr.apply("3456789")).isEqualTo("3456789");
        assertThat(mr.apply("3-45678-9")).isEqualTo("3-45678-9");
        assertThat(mr.apply("456789")).isEqualTo("5678");
        assertThat(mr.apply("75-6-4")).isEqualTo("6");
        assertThat(mr.apply("345")).isEqualTo("345");
        assertThat(mr.apply("abc-25-xyz")).isEqualTo("25");
        assertThat(mr.apply("abc-6to4-xyz")).isEqualTo("6to4");
        assertThat(mr.apply("411-867-5309")).isEqualTo("411 867 5309");

    }
}

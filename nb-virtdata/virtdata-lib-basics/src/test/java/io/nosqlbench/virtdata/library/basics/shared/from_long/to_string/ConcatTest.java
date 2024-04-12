/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToHexString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConcatTest {

    @Test
    public void testEmptyString() {
        Concat p = new Concat("");
        assertThat(p.apply(3L)).isEqualTo("");
    }

    @Test
    public void testMismatchedInserts() {
        Concat c = new Concat("{}");
        assertThat(c.apply(3L)).isEqualTo("v:3");
    }

    @Test
    public void testOnlyLiteral() {
        Concat l = new Concat(Long::sum, "literal");
        assertThat(l.apply(3L)).isEqualTo("literal");
    }
    @Test
    public void testSimpleValue() {
        Concat p = new Concat(Long::sum, "{}", new NumberNameToString());
        assertThat(p.apply(1)).isEqualTo("one");
    }

    @Test
    public void testFixedCycle() {
        Concat p = new Concat((c, s) -> c, "{}-{}", new NumberNameToString());
        assertThat(p.apply(3L)).isEqualTo("three-three");
    }

    @Test
    public void testSteppedCycle() {
        Concat p = new Concat(Long::sum, "{}-{}", new NumberNameToString());
        assertThat(p.apply(3L)).isEqualTo("three-four");
    }


    @Test
    public void testConcatChained() {
        ConcatChained cc = new ConcatChained("{}-{}", new ToHexString());
        assertThat(cc.apply(1)).isEqualTo("5752fae69d1653da-7dfbf78ca62528b5");
    }

}

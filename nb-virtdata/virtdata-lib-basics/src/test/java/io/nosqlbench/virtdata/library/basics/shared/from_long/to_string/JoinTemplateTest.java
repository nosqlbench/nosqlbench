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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Mod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinTemplateTest {

    @Test
    public void testBasicJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate("__", new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(3);
        assertThat(v).isEqualTo("three__four");
    }

    @Test
    public void testPrefixSuffixJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate("<","__", ">",new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(3);
        assertThat(v).isEqualTo("<three__four>");
    }

    @Test
    public void testIterOpFunctionJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate(new Mod(5L), "<", "__",">",
                new NumberNameToString(), new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(17);
        assertThat(v).isEqualTo("<two__three__four>");

    }

}

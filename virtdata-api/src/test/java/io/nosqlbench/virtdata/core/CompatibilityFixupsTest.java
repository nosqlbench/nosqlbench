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

package io.nosqlbench.virtdata.core;

import io.nosqlbench.virtdata.core.bindings.CompatibilityFixups;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompatibilityFixupsTest {

    @Test
    public void testInlineChange() {
        assertThat(CompatibilityFixups.fixup("Hash(); uniform_integer(0,1000000000); ToString() -> String"))
        .isEqualTo("Hash(); Uniform(0,1000000000,'hash','interpolate'); ToString() -> String");
    }

    @Test
    public void testFixupModifiers() {
        assertThat(CompatibilityFixups.fixup("compute_levy(ASDF)")).isEqualTo("Levy(ASDF,'hash','compute')");
        assertThat(CompatibilityFixups.fixup("interpolate_levy(ASDF)")).isEqualTo("Levy(ASDF,'hash','interpolate')");
        assertThat(CompatibilityFixups.fixup("mapto_levy(ASDF)")).isEqualTo("Levy(ASDF,'map','interpolate')");
        assertThat(CompatibilityFixups.fixup("hashto_levy(ASDF)")).isEqualTo("Levy(ASDF,'hash','interpolate')");
    }

    @Test
    public void testFixupNames() {
        assertThat(CompatibilityFixups.fixup("gamma(foo)")).isEqualTo("Gamma(foo,'hash','interpolate')");
        assertThat(CompatibilityFixups.fixup("mapto_uniform_integer(foo)")).isEqualTo("Uniform(foo,'map','interpolate')");
        assertThat(CompatibilityFixups.fixup("hashto_uniform_real(foo)")).isEqualTo("Uniform(foo,'hash','interpolate')");
    }

    @Test
    public void testParsingSanity() {
        assertThat(CompatibilityFixups.fixup("long -> Add(5) -> long")).isEqualTo("long -> Add(5) -> long");
    }
}

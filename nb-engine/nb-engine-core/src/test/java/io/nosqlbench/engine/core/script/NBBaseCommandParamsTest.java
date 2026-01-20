/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.core.script;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Tag("unit")
public class NBBaseCommandParamsTest {

    @Test
    public void testThatNullOverridesKeyThrowsBasicError() {
        NBCommandParams p = new NBCommandParams();
        p.put("a","b");
        p.withDefaults(Map.of("c","d"));
        HashMap<String, String> overrides = new HashMap<>();
        overrides.put(null,"test");
        assertThatExceptionOfType(BasicError.class)
                .isThrownBy(() -> p.withOverrides(overrides));
    }

}

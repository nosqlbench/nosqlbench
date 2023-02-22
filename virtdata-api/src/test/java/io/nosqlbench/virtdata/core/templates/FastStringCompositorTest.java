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

package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class FastStringCompositorTest {

    @Test
    @Disabled // Needs to have annotation processor run in test scope first
    public void testFastStringCompositor() {
        String rawTpl = "template {b1}, {{TestValue(5)}}";
        Map<String, String> bindings = Map.of("b1", "TestIdentity()");
        ParsedTemplateString ptpl = new ParsedTemplateString(rawTpl, bindings);
        StringCompositor fsc = new StringCompositor(ptpl,Map.of());
        System.out.println(fsc);
    }

}

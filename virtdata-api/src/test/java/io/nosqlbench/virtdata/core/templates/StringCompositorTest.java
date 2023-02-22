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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCompositorTest {

    @Test
    public void testShouldMatchSpanOnly() {
        ParsedTemplateString pt = new ParsedTemplateString("A\\{ {one}two", Map.of());
        assertThat(pt.getSpans()).containsExactly("A\\{ ", "one", "two");
    }

    @Test
    public void testShouldNotMatchEscaped() {
        ParsedTemplateString pt = new ParsedTemplateString("A\\{{B}C",Map.of());
        assertThat(pt.getSpans()).containsExactly("A\\{","B","C");
    }

//    @Test
//    public void testShoulsIgnoreExplicitExcapes() {
//        StringCompositor c = new StringCompositor("A");
//        String[] spans = c.parseTemplate("A\\{B}C");
//        assertThat(spans).containsExactly("A\\{B}C");
//    }
}

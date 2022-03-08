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

package io.nosqlbench.engine.api.templating;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateListTest {

    @Test
    public void testTemplateListLiterals() {
        ParsedTemplateList ptl = new ParsedTemplateList(List.of("a","b"), Map.of(),List.of());
        List<?> made = ptl.apply(2L);
        assertThat(made).isEqualTo(List.of("a","b"));
    }

//    @Test
//    public void testTemplateListReferences() {
//        ParsedTemplateList ptl = new ParsedTemplateList(List.of("{a}","{b}"), Map.of("a","TestingRepeater(2)","b","TestingRepeater(4)"),List.of());
//        List<?> made = ptl.apply(2L);
//        assertThat(made).isEqualTo(List.of("a","b"));
//    }

}

/*
 * Copyright (c) 2023 nosqlbench
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

import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateMapTest {

    @Test
    public void testParsedTemplateMap() {
        ParsedTemplateMap ptm = new ParsedTemplateMap("name1", Map.of("string1", "string2"), Map.of(), List.of());
        assertThat(ptm.getOpFieldNames()).isEqualTo(Set.of("string1"));
    }

    @Test
    public void testTakeAsNamedTemplates() {
        ParsedTemplateMap ptm = new ParsedTemplateMap(
            "test2",
            new LinkedHashMap<String,Object>(Map.of(
                "astring","astring",
                "alist",List.of("listentry1","listentry2"),
                "amap", Map.of("entry1","val1", "entry2", "val2")
            )),
            new LinkedHashMap<>(Map.of()),
            List.of(Map.of())
            );
        Map<String, ParsedTemplateString> ofString = ptm.takeAsNamedTemplates("astring");
        assertThat(ofString).containsKey("test2-verifier-0");
        Map<String, ParsedTemplateString> ofList = ptm.takeAsNamedTemplates("alist");
        assertThat(ofList).containsKey("test2-verifier-0");
        assertThat(ofList).containsKey("test2-verifier-1");
        Map<String, ParsedTemplateString> ofMap = ptm.takeAsNamedTemplates("amap");
        assertThat(ofMap).containsKey("test2-verifier-entry1");
        assertThat(ofMap).containsKey("test2-verifier-entry2");
        // TODO: Get actual testing bindings into this example

    }

}

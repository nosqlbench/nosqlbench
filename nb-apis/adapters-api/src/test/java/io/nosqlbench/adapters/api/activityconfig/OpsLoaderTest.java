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

package io.nosqlbench.adapters.api.activityconfig;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDoc;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpsLoaderTest {

    @Test
    public void testTemplateVarSubstitution() {
        OpsDocList opsDocs = OpsLoader.loadPath("activities/template_vars", new HashMap<>(),"src/test/resources");
        assertThat(opsDocs).isNotNull();
        List<OpsDoc> docs = opsDocs.getStmtDocs();
        assertThat(docs).hasSize(1);
        OpsDoc opsDoc = docs.get(0);
        Map<String, String> bindings = opsDoc.getBindings();
        assertThat(bindings).isEqualTo(Map.of(
            "b1a","Prefix(\"prefix\")",
            "b1b","Prefix(\"prefix\")",
            "b2a","Suffix(\"suffix\")",
            "b2b","Suffix(\"suffix\")"));
    }

    @Test
    public void testInvalidYamlProperties() {
        Exception caught = null;
        try {
            OpsLoader.loadPath("activities/invalid_prop", Map.of(),"src/test/resources");
        } catch (Exception e) {
            caught = e;
        }
        assertThat(caught).isNotNull();
    }



}

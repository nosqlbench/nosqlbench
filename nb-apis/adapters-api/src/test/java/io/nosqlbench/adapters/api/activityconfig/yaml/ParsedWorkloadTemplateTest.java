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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedWorkloadTemplateTest {
    private static final Logger logger = LogManager.getLogger(ParsedWorkloadTemplateTest.class);
    private static OpsDocList doclist;

    @BeforeAll
    public static void testLoadYaml() {
        doclist = OpsLoader.loadPath("testdocs/bindings.yaml", Map.of());
    }

    @Test
    public void testBasicParser() {
        OpsBlock block0 = doclist.getStmtDocs().get(0).getBlocks().get(0);
        OpTemplate stmtDef0 = block0.getOps().get(0);
        ParsedTemplateString parsed0 = stmtDef0.getParsed().orElseThrow();
        assertThat(parsed0.getMissing()).containsExactly("delta");
        assertThat(parsed0.hasError()).isTrue();

        OpsBlock block1 = doclist.getStmtDocs().get(0).getBlocks().get(1);
        OpTemplate stmtDef1 = block1.getOps().get(0);
        ParsedTemplateString parsed1 = stmtDef1.getParsed().orElseThrow();
        assertThat(parsed1.getMissing()).containsExactly();
        assertThat(parsed1.hasError()).isFalse();
    }

    @Test
    public void testMultipleBindingUsage() {
        OpsBlock block2 = doclist.getStmtDocs().get(0).getBlocks().get(2);

        OpTemplate stmtDef0 = block2.getOps().get(0);
        ParsedTemplateString parsed0 = stmtDef0.getParsed().orElseThrow();
        assertThat(parsed0.getMissing()).isEmpty();
        assertThat(parsed0.hasError()).isFalse();

        OpTemplate stmtDef1 = block2.getOps().get(1);
        ParsedTemplateString parsed1 = stmtDef1.getParsed().orElseThrow();
        assertThat(parsed1.getMissing()).isEmpty();
        assertThat(parsed1.hasError()).isFalse();
    }

}

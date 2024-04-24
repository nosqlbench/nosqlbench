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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsBlock;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDoc;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpEscapingTest {

    private final static Logger logger = LogManager.getLogger(OpEscapingTest.class);
    private static List<OpTemplate> defs;

    @BeforeAll
    public static void testLayering() {

        OpsDocList all = OpsLoader.loadPath("testdocs/escaped_ops.yaml", Map.of());
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(1);
        OpsDoc doc1 = all.getStmtDocs().get(0);

//        assertThat(doc1.getName()).isEqualTo("doc1");
        assertThat(doc1.getBlocks()).hasSize(1);

        OpsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getBindings()).hasSize(0);
        assertThat(block1.getTags()).hasSize(0);
        assertThat(block1.getOps()).hasSize(3);

        defs = block1.getOps();
    }

    @Test
    public void testBackslashEscape() {
        String s1 = defs.get(0).getStmt().orElseThrow();
        assertThat(s1).isEqualTo("This is a \"statement\"");
    }

    @Test
    public void testBackslashInBlock() {
        String s2 = defs.get(1).getStmt().orElseThrow();
        assertThat(s2).isEqualTo("This is a \\\"statement\\\".\n");
    }

    @Test
    public void testTripleQuotesInBlock() {
        String s3 = defs.get(2).getStmt().orElseThrow();
        assertThat(s3).isEqualTo("This is a \"\"\"statement\"\"\".\n");
    }

}

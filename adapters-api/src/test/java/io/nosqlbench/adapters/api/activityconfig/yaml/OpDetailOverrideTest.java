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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpDetailOverrideTest {
    private static final Logger logger = LogManager.getLogger(OpDetailOverrideTest.class);

    @Test
    public void testStmtOverrides() {

        OpsDocList doclist = OpsLoader.loadPath("testdocs/op_details.yaml", Map.of());

        assertThat(doclist).isNotNull();

        assertThat(doclist.getStmtDocs()).hasSize(1);
        OpsDoc doc1 = doclist.getStmtDocs().get(0);

        assertThat(doc1.getBlocks()).hasSize(2);
        OpsBlock doc1block0 = doc1.getBlocks().get(0);
        assertThat(doc1block0.getOps().size()).isEqualTo(1);
        OpTemplate s = doc1block0.getOps().get(0);
        assertThat(s.getName()).isEqualTo("block0--stmt1");
        assertThat(s.getStmt()).contains("globalstatement1");
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);
        assertThat(s.getTags()).isEqualTo(Map.of("block","block0","global_tag1","tag value","name","block0--stmt1"));

        OpsBlock doc1block1 = doc1.getBlocks().get(1);
        List<OpTemplate> ops = doc1block1.getOps();
        assertThat(ops).hasSize(4);

        s = ops.get(0);
        assertThat(s.getName()).isEqualTo("testblock1--stmt1");
        assertThat(s.getStmt()).contains("astatement1");
        assertThat(s.getTags()).isEqualTo(Map.of("block","testblock1","global_tag1","tag value","name","testblock1--stmt1"));
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

        s = ops.get(1);
        assertThat(s.getName()).isEqualTo("testblock1--s2name");
        assertThat(s.getStmt()).contains("s2statement data");
        assertThat(s.getTags()).isEqualTo(Map.of("block","testblock1","global_tag1","tag value","name","testblock1--s2name"));
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

        s = ops.get(2);
        assertThat(s.getName()).isEqualTo("testblock1--s3");
        assertThat(s.getStmt()).contains("statement three");
        assertThat(s.getTags()).containsEntry("tname1", "tval1");
        assertThat(s.getTags()).containsEntry("global_tag1", "tag value");
        assertThat(s.getBindings()).hasSize(3);
        assertThat(s.getParams()).hasSize(2);

        s = ops.get(3);
        assertThat(s.getName()).isEqualTo("testblock1--s4");
        assertThat(s.getStmt()).contains("statement 4");
        assertThat(s.getTags()).isEqualTo(Map.of("block","testblock1","global_tag1","tag value","name","testblock1--s4"));
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

    }
}

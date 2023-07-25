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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpDefTest {

    private final static Logger logger = LogManager.getLogger(OpDefTest.class);

    @Test
    public void testLayering() {

        OpsDocList all = OpsLoader.loadPath("testdocs/docs_blocks_ops.yaml", Map.of());
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(2);
        OpsDoc doc1 = all.getStmtDocs().get(0);
        assertThat(doc1.getName()).isEqualTo("doc1");
        assertThat(doc1.getBlocks()).hasSize(1);
        OpsDoc doc2 = all.getStmtDocs().get(1);
        assertThat(doc2.getBlocks()).hasSize(2);

        OpsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getBindings()).hasSize(2);
        assertThat(block1.getName()).isEqualTo("doc1--block0");
        assertThat(block1.getTags()).hasSize(1);

        OpsBlock block21 = doc2.getBlocks().get(0);
        OpsBlock block22 = doc2.getBlocks().get(1);

        assertThat(block21.getName()).isEqualTo("doc2--block1");
        assertThat(block21.getTags()).hasSize(3);

        assertThat(block22.getName()).isEqualTo("doc2--block2");
        assertThat(block22.getTags()).hasSize(2);
        assertThat(block22.getTags().get("root1")).isEqualTo("value23");
    }

    @Test
    public void testStatementRendering() {
        OpsDocList all = OpsLoader.loadPath("testdocs/docs_blocks_ops.yaml", Map.of());
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(2);
        OpsDoc doc1 = all.getStmtDocs().get(0);
        OpsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("doc1--block0");
        List<OpTemplate> ops = block1.getOps();
        assertThat(ops).hasSize(2);
        OpTemplate sdef1 = ops.get(0);
        assertThat(sdef1.getName()).isEqualTo("doc1--block0--stmt1");
        assertThat(ops.get(0).getOp()).contains(Map.of("stmt","s1"));
    }

    @Test
    public void testConsumableMapState() {
        OpsDocList all = OpsLoader.loadPath("testdocs/docs_blocks_ops.yaml", Map.of());
        List<OpsDoc> docs = all.getStmtDocs();
        OpsDoc block1 = docs.get(1);
        List<OpTemplate> stmts = block1.getOpTemplates();
        OpTemplate stmt0 = stmts.get(0);
        OpTemplate stmt1 = stmts.get(1);
        assertThat(stmt0.getParams()).containsAllEntriesOf(Map.of("timeout", 23423, "foobar", "baz"));
        assertThat(stmt1.getParams()).containsAllEntriesOf(Map.of("timeout", 23423, "foobar", "baz"));
        stmt0.removeParamOrDefault("timeout", 23423);
        assertThat(stmt0.getParams()).containsAllEntriesOf(Map.of("foobar", "baz"));
        assertThat(stmt1.getParams()).containsAllEntriesOf(Map.of("timeout", 23423, "foobar", "baz"));
    }

    @Test
    public void testMapOfMaps() {
        OpsDocList all = OpsLoader.loadPath("testdocs/op_variants.yaml", Map.of());
        List<OpsDoc> docs = all.getStmtDocs();
        OpsDoc doc0 = docs.get(0);
        assertThat(doc0.getName()).isEqualTo("map-of-maps");
        assertThat(doc0.getBlocks()).hasSize(1);
        OpsBlock block1 = doc0.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("map-of-maps--block0");
        assertThat(block1.getOps()).hasSize(2);
        OpTemplate op0 = block1.getOps().get(0);
        assertThat(op0.getName()).isEqualTo("map-of-maps--block0--s3");
        assertThat(op0.getOp()).contains(Map.of("p1","v7","p2","v8"));
        assertThat(op0.getParams()).hasSize(0);
        assertThat(op0.getParams()).hasSize(0);
        OpTemplate op1 = block1.getOps().get(1);
        assertThat(op1.getParams()).containsAllEntriesOf(Map.of());
        assertThat(op1.getName()).isEqualTo("map-of-maps--block0--s2");
        assertThat(op1.getOp()).contains(Map.of("stmt","statement2"));
    }

    @Test
    public void testBasicStringStmt() {
        OpsDocList all = OpsLoader.loadPath("testdocs/op_variants.yaml", Map.of());
        List<OpsDoc> docs = all.getStmtDocs();
        OpsDoc doc1 = docs.get(1);
        assertThat(doc1.getName()).isEqualTo("string-statement");
        assertThat(doc1.getBlocks()).hasSize(1);
        OpsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("string-statement--block0");
        assertThat(block1.getOps()).hasSize(1);
        OpTemplate op0 = block1.getOps().get(0);
        assertThat(op0.getName()).isEqualTo("string-statement--block0--stmt1");
        assertThat(op0.getOp()).contains(Map.of("stmt","test statement"));
    }

    @Test
    public void testListOfNamedMap() {
        OpsDocList all = OpsLoader.loadPath("testdocs/op_variants.yaml", Map.of());
        List<OpsDoc> docs = all.getStmtDocs();
        OpsDoc doc2 = docs.get(2);
        assertThat(doc2.getName()).isEqualTo("list-of-named-map");
        assertThat(doc2.getBlocks()).hasSize(1);
        OpsBlock block1 = doc2.getBlocks().get(0);
        assertThat(block1.getOps()).hasSize(1);
        OpTemplate op0 = block1.getOps().get(0);
        System.out.println(op0.getParams());

        assertThat(op0.getName()).isEqualTo("list-of-named-map--block1--s1");
        assertThat(op0.getOp()).contains(Map.of("p1","v1","p2","v2"));
//        System.out.println("here");
        // TODO: This needs to be clarified and the logic made less ambiguous
//        assertThat(op0.getParams()).hasSize(1);
//        assertThat(op0.getParams()).containsExactlyEntriesOf(Map.of("p1", "v2", "p2", "v2", "p3", "v3"));
    }


}

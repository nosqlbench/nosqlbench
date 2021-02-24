/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsBlock;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDoc;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpDefTest {

    private final static Logger logger = LogManager.getLogger(OpDefTest.class);

    @Test
    public void testLayering() {

        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/docs_blocks_stmts.yaml");
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(2);
        StmtsDoc doc1 = all.getStmtDocs().get(0);
        assertThat(doc1.getName()).isEqualTo("doc1");
        assertThat(doc1.getBlocks()).hasSize(1);
        StmtsDoc doc2 = all.getStmtDocs().get(1);
        assertThat(doc2.getBlocks()).hasSize(2);

        StmtsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getBindings()).hasSize(2);
        assertThat(block1.getName()).isEqualTo("doc1--block0");
        assertThat(block1.getTags()).hasSize(1);

        StmtsBlock block21 = doc2.getBlocks().get(0);
        StmtsBlock block22 = doc2.getBlocks().get(1);

        assertThat(block21.getName()).isEqualTo("doc2--block1");
        assertThat(block21.getTags()).hasSize(3);

        assertThat(block22.getName()).isEqualTo("doc2--block2");
        assertThat(block22.getTags()).hasSize(2);
        assertThat(block22.getTags().get("root1")).isEqualTo("value23");
    }

    @Test
    public void testStatementRendering() {
        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/docs_blocks_stmts.yaml");
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(2);
        StmtsDoc doc1 = all.getStmtDocs().get(0);
        StmtsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("doc1--block0");
        List<OpTemplate> assys = block1.getOps();
        assertThat(assys).hasSize(2);
        OpTemplate sdef1 = assys.get(0);
        assertThat(sdef1.getName()).isEqualTo("doc1--block0--stmt1");
        assertThat(assys.get(0).getStmt()).isEqualTo("s1");
    }

    @Test
    public void testConsumableMapState() {
        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/docs_blocks_stmts.yaml");
        List<StmtsDoc> docs = all.getStmtDocs();
        StmtsDoc block1 = docs.get(1);
        List<OpTemplate> stmts = block1.getStmts();
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
        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/statement_variants.yaml");
        List<StmtsDoc> docs = all.getStmtDocs();
        StmtsDoc doc0 = docs.get(0);
        assertThat(doc0.getName()).isEqualTo("map-of-maps");
        assertThat(doc0.getBlocks()).hasSize(1);
        StmtsBlock block1 = doc0.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("map-of-maps--block0");
        assertThat(block1.getOps()).hasSize(2);
        OpTemplate op0 = block1.getOps().get(0);
        assertThat(op0.getName()).isEqualTo("map-of-maps--block0--s3");
        assertThat(op0.getParams()).hasSize(2);
        assertThat(op0.getParams()).containsAllEntriesOf(Map.of("p1", "v7", "p2", "v8"));
        OpTemplate op1 = block1.getOps().get(1);
        assertThat(op1.getParams()).containsAllEntriesOf(Map.of());
        assertThat(op1.getName()).isEqualTo("map-of-maps--block0--s2");
        assertThat(op1.getStmt()).isEqualTo("statement2");
    }

    @Test
    public void testBasicStringStmt() {
        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/statement_variants.yaml");
        List<StmtsDoc> docs = all.getStmtDocs();
        StmtsDoc doc1 = docs.get(1);
        assertThat(doc1.getName()).isEqualTo("string-statement");
        assertThat(doc1.getBlocks()).hasSize(1);
        StmtsBlock block1 = doc1.getBlocks().get(0);
        assertThat(block1.getName()).isEqualTo("string-statement--block0");
        assertThat(block1.getOps()).hasSize(1);
        OpTemplate op0 = block1.getOps().get(0);
        assertThat(op0.getName()).isEqualTo("string-statement--block0--stmt1");
        assertThat(op0.getStmt()).isEqualTo("test statement");
    }

    @Test
    public void testListOfNamedMap() {
        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/statement_variants.yaml");
        List<StmtsDoc> docs = all.getStmtDocs();
        StmtsDoc doc2 = docs.get(2);
        assertThat(doc2.getName()).isEqualTo("list-of-named-map");
        assertThat(doc2.getBlocks()).hasSize(1);
        StmtsBlock block1 = doc2.getBlocks().get(0);
        assertThat(block1.getOps()).hasSize(1);
        OpTemplate op0 = block1.getOps().get(0);
        assertThat(op0.getName()).isEqualTo("list-of-named-map--block0--s1");
        assertThat(op0.getStmt()).isNull();
        // TODO: This needs to be clarified and the logic made less ambiguous
//        assertThat(op0.getParams()).hasSize(1);
//        assertThat(op0.getParams()).containsExactlyEntriesOf(Map.of("p1", "v2", "p2", "v2", "p3", "v3"));
    }


}

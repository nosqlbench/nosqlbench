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

public class StmtDefTest {

    private final static Logger logger = LogManager.getLogger(StmtDefTest.class);

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
        List<OpTemplate> assys = block1.getStmts();
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

}

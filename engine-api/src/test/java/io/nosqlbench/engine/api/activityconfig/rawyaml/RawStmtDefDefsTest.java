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
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsBlock;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDoc;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RawStmtDefDefsTest {

    private final static Logger logger = LoggerFactory.getLogger(RawStmtDefDefsTest.class);

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
        List<StmtDef> assys = block1.getStmts();
        assertThat(assys).hasSize(2);
        StmtDef sdef1 = assys.get(0);
        assertThat(sdef1.getName()).isEqualTo("doc1--block0--stmt1");
        assertThat(assys.get(0).getStmt()).isEqualTo("s1");
    }

}

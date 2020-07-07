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

package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StmtDetailOverrideTest {
    private static final Logger logger = LoggerFactory.getLogger(StmtDetailOverrideTest.class);

    @Test
    public void testStmtOverrides() {

        StmtsDocList doclist = StatementsLoader.loadPath(logger, "testdocs/stmt_details.yaml");

        assertThat(doclist).isNotNull();

        assertThat(doclist.getStmtDocs()).hasSize(1);
        StmtsDoc doc1 = doclist.getStmtDocs().get(0);

        assertThat(doc1.getBlocks()).hasSize(2);
        StmtsBlock doc1block0 = doc1.getBlocks().get(0);
        assertThat(doc1block0.getStmts().size()).isEqualTo(1);
        OpTemplate s = doc1block0.getStmts().get(0);
        assertThat(s.getName()).isEqualTo("block0--stmt1");
        assertThat(s.getStmt()).isEqualTo("globalstatement1");
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);
        assertThat(s.getTags()).hasSize(1);

        StmtsBlock doc1block1 = doc1.getBlocks().get(1);
        List<OpTemplate> stmts = doc1block1.getStmts();
        assertThat(stmts).hasSize(4);

        s = stmts.get(0);
        assertThat(s.getName()).isEqualTo("testblock1--stmt1");
        assertThat(s.getStmt()).isEqualTo("astatement1");
        assertThat(s.getTags()).hasSize(1);
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

        s = stmts.get(1);
        assertThat(s.getName()).isEqualTo("testblock1--s2name");
        assertThat(s.getStmt()).isEqualTo("s2statement data");
        assertThat(s.getTags()).hasSize(1);
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

        s = stmts.get(2);
        assertThat(s.getName()).isEqualTo("testblock1--s3");
        assertThat(s.getStmt()).isEqualTo("statement three");
        assertThat(s.getTags()).containsEntry("tname1", "tval1");
        assertThat(s.getTags()).containsEntry("global_tag1", "tag value");
        assertThat(s.getBindings()).hasSize(3);
        assertThat(s.getParams()).hasSize(3);

        s = stmts.get(3);
        assertThat(s.getName()).isEqualTo("testblock1--s4");
        assertThat(s.getStmt()).isEqualTo("statement 4");
        assertThat(s.getTags()).hasSize(1);
        assertThat(s.getBindings()).hasSize(1);
        assertThat(s.getParams()).hasSize(1);

    }
}

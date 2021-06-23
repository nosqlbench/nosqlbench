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
import io.nosqlbench.engine.api.activityconfig.yaml.*;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StmtEscapingTest {

    private final static Logger logger = LogManager.getLogger(StmtEscapingTest.class);
    private static List<OpTemplate> defs;

    @BeforeAll
    public static void testLayering() {

        StmtsDocList all = StatementsLoader.loadPath(logger, "testdocs/escaped_stmts.yaml");
        assertThat(all).isNotNull();
        assertThat(all.getStmtDocs()).hasSize(1);
        StmtsDoc doc1 = all.getStmtDocs().get(0);

//        assertThat(doc1.getName()).isEqualTo("doc1");
        assertThat(doc1.getBlocks()).hasSize(1);

        StmtsBlock block1 = doc1.getBlocks().get(0);
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

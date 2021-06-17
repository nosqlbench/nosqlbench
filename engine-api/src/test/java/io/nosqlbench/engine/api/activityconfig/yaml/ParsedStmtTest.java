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
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedStmtTest {
    private static final Logger logger = LogManager.getLogger(ParsedStmtTest.class);
    private static StmtsDocList doclist;

    @BeforeAll
    public static void testLoadYaml() {
        doclist = StatementsLoader.loadPath(logger, "testdocs/bindings.yaml");
    }

    @Test
    public void testBasicParser() {
        StmtsBlock block0 = doclist.getStmtDocs().get(0).getBlocks().get(0);
        OpTemplate stmtDef0 = block0.getOps().get(0);
        ParsedStmt parsed0 = stmtDef0.getParsed();
        assertThat(parsed0.getExtraBindings()).containsExactly("alpha","gamma");
        assertThat(parsed0.getMissingBindings()).containsExactly("delta");
        assertThat(parsed0.hasError()).isTrue();

        StmtsBlock block1 = doclist.getStmtDocs().get(0).getBlocks().get(1);
        OpTemplate stmtDef1 = block1.getOps().get(0);
        ParsedStmt parsed1 = stmtDef1.getParsed();
        assertThat(parsed1.getExtraBindings()).containsExactly();
        assertThat(parsed1.getMissingBindings()).containsExactly();
        assertThat(parsed1.hasError()).isFalse();
    }

    @Test
    public void testMultipleBindingUsage() {
        StmtsBlock block2 = doclist.getStmtDocs().get(0).getBlocks().get(2);

        OpTemplate stmtDef0 = block2.getOps().get(0);
        ParsedStmt parsed0 = stmtDef0.getParsed();
        assertThat(parsed0.getMissingBindings()).isEmpty();
        assertThat(parsed0.hasError()).isFalse();

        OpTemplate stmtDef1 = block2.getOps().get(1);
        ParsedStmt parsed1 = stmtDef1.getParsed();
        assertThat(parsed1.getMissingBindings().isEmpty());
        assertThat(parsed1.hasError()).isFalse();
    }

}

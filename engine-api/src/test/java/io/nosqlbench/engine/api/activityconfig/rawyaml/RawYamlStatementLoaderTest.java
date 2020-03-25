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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class RawYamlStatementLoaderTest {
    private final static Logger logger = LoggerFactory.getLogger(RawYamlStatementLoaderTest.class);

    @Test
    public void testLoadPropertiesBlock() {
        RawYamlStatementLoader ysl = new RawYamlStatementLoader();
        RawStmtsDocList rawBlockDocs = ysl.load(logger, "testdocs/rawblock.yaml");
        assertThat(rawBlockDocs.getStmtsDocs()).hasSize(1);
        RawStmtsDoc rawBlockDoc = rawBlockDocs.getStmtsDocs().get(0);
        assertThat(rawBlockDoc.getRawStmtDefs()).hasSize(1);
        assertThat(rawBlockDoc.getBindings()).hasSize(1);
        assertThat(rawBlockDoc.getName()).isEqualTo("name");
        assertThat(rawBlockDoc.getTags()).hasSize(1);
        assertThat(rawBlockDoc.getParams()).hasSize(1);
    }

    @Test
    public void testLoadFullFormat() {
        RawYamlStatementLoader ysl = new RawYamlStatementLoader();
        RawStmtsDocList erthing = ysl.load(logger, "testdocs/docs_blocks_stmts.yaml");
        List<RawStmtsDoc> rawStmtsDocs = erthing.getStmtsDocs();
        assertThat(rawStmtsDocs).hasSize(2);
        RawStmtsDoc rawStmtsDoc = rawStmtsDocs.get(0);
        List<RawStmtsBlock> blocks = rawStmtsDoc.getBlocks();
        assertThat(rawStmtsDoc.getName()).isEqualTo("doc1");
        assertThat(blocks).hasSize(1);
        RawStmtsBlock rawStmtsBlock = blocks.get(0);
        assertThat(rawStmtsBlock.getName()).isEqualTo("block0");
    }

    @Test
    public void testLoadScenarios() {
        RawYamlStatementLoader ysl = new RawYamlStatementLoader();
        RawStmtsDocList erthing = ysl.load(logger, "testdocs/docs_blocks_stmts.yaml");
        List<RawStmtsDoc> rawStmtsDocs = erthing.getStmtsDocs();
        assertThat(rawStmtsDocs).hasSize(2);
        RawStmtsDoc rawStmtsDoc = rawStmtsDocs.get(0);
        List<RawStmtsBlock> blocks = rawStmtsDoc.getBlocks();
        RawScenarios rawScenarios = rawStmtsDoc.getRawScenarios();
        assertThat(rawScenarios.getScenarioNames()).containsExactly("default", "schema-only");
        List<String> defaultScenario = rawScenarios.getNamedScenario("default");
        assertThat(defaultScenario).containsExactly("run driver=stdout alias=step1","run driver=stdout alias=step2");
        List<String> schemaOnlyScenario = rawScenarios.getNamedScenario("schema-only");
        assertThat(schemaOnlyScenario).containsExactly("run driver=blah tags=phase:schema");

        assertThat(rawStmtsDoc.getName()).isEqualTo("doc1");
        assertThat(blocks).hasSize(1);
        RawStmtsBlock rawStmtsBlock = blocks.get(0);
        assertThat(rawStmtsBlock.getName()).isEqualTo("block0");



    }

}

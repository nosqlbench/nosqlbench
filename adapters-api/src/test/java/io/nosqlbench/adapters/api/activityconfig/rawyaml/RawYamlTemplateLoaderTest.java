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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RawYamlTemplateLoaderTest {
    private final static Logger logger = LogManager.getLogger(RawYamlTemplateLoaderTest.class);

    @Test
    public void testLoadPropertiesBlock() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList rawBlockDocs = ysl.loadPath("testdocs/rawblock.yaml");
        assertThat(rawBlockDocs.getOpsDocs()).hasSize(1);
        RawOpsDoc rawBlockDoc = rawBlockDocs.getOpsDocs().get(0);
        assertThat(rawBlockDoc.getRawOpDefs()).hasSize(1);
        assertThat(rawBlockDoc.getBindings()).hasSize(1);
        assertThat(rawBlockDoc.getName()).isEqualTo("name");
        assertThat(rawBlockDoc.getTags()).hasSize(1);
        assertThat(rawBlockDoc.getParams()).hasSize(1);
    }

    @Test
    public void testLoadFullFormat() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList erthing = ysl.loadPath("testdocs/docs_blocks_ops.yaml");
        List<RawOpsDoc> rawOpsDocs = erthing.getOpsDocs();
        Assertions.assertThat(rawOpsDocs).hasSize(2);
        RawOpsDoc rawOpsDoc = rawOpsDocs.get(0);
        List<RawOpsBlock> blocks = rawOpsDoc.getBlocks();
        assertThat(rawOpsDoc.getName()).isEqualTo("doc1");
        assertThat(blocks).hasSize(1);
        RawOpsBlock rawOpsBlock = blocks.get(0);
        assertThat(rawOpsBlock.getName()).isEqualTo("block0");
    }

    @Test
    public void testLoadScenarios() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList erthing = ysl.loadPath("testdocs/docs_blocks_ops.yaml");
        List<RawOpsDoc> rawOpsDocs = erthing.getOpsDocs();
        Assertions.assertThat(rawOpsDocs).hasSize(2);
        RawOpsDoc rawOpsDoc = rawOpsDocs.get(0);
        List<RawOpsBlock> blocks = rawOpsDoc.getBlocks();
        assertThat(rawOpsDoc.getDesc()).isEqualTo(
            "a quintessential description - this is superseded by dedicated specification tests and will be removed");

        RawScenarios rawScenarios = rawOpsDoc.getRawScenarios();
        assertThat(rawScenarios.getScenarioNames()).containsExactly("default", "schema-only");
        Map<String, String> defaultScenario = rawScenarios.getNamedScenario("default");
        assertThat(defaultScenario.keySet())
            .containsExactly("000","001");
        assertThat(defaultScenario.values())
            .containsExactly("run driver=stdout alias=step1","run driver=stdout alias=step2");
        Map<String, String> schemaOnlyScenario = rawScenarios.getNamedScenario("schema-only");
        assertThat(schemaOnlyScenario.keySet())
            .containsExactly("000");
        assertThat(schemaOnlyScenario.values())
            .containsExactly("run driver=blah tags=block:\"schema.*\"");

        assertThat(rawOpsDoc.getName()).isEqualTo("doc1");
        assertThat(blocks).hasSize(1);
        RawOpsBlock rawOpsBlock = blocks.get(0);
        assertThat(rawOpsBlock.getName()).isEqualTo("block0");
    }

    @Test
    public void testErrorMsg() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList erthing = ysl.loadPath("testdocs/badyamlfile.yaml");
    }

}

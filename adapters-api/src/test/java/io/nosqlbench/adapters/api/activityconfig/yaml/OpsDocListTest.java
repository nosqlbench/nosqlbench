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
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpsDocListTest {

    private static final Logger logger = LogManager.getLogger(OpsDocListTest.class);
    private static OpsDocList doclist;

    private final LinkedHashMap<String, String> doc0bindings = new LinkedHashMap<String, String>() {{
        put("b2", "b2d");
        put("b1", "b1d");
    }};

    private final LinkedHashMap<String, String> doc0params = new LinkedHashMap<String,String>() {{
        put("param1","value1");
    }};

    private final LinkedHashMap<String, String> doc0tags = new LinkedHashMap<String,String>() {{
        put("atagname","atagvalue");
    }};


    @BeforeAll
    public static void testLoadYaml() {
        doclist = OpsLoader.loadPath("testdocs/docs_blocks_ops.yaml", Map.of());
    }

    @Test
    public void testBlocksInheritDocData() {
        assertThat(doclist).isNotNull();
        assertThat(doclist.getStmtDocs()).hasSize(2);
        OpsDoc doc1 = doclist.getStmtDocs().get(0);

        assertThat(doc1.getBlocks()).hasSize(1);
        OpsBlock doc1block0 = doc1.getBlocks().get(0);

        assertThat(doc1.getTags()).isEqualTo(doc0tags);
        assertThat(doc1.getBindings()).isEqualTo(doc0bindings);
        assertThat(doc1.getParams()).isEqualTo(doc0params);

        assertThat(doc1block0.getName()).isEqualTo("doc1--block0");
        assertThat(doc1block0.getBindings()).containsExactly(MapEntry.entry("b2","b2d"),MapEntry.entry("b1","b1d"));
        assertThat(doc1block0.getParams()).containsExactly(MapEntry.entry("param1","value1"));
        assertThat(doc1block0.getTags()).containsExactly(MapEntry.entry("atagname","atagvalue"));

    }

    @Test
    public void testStmtInheritsBlockData() {
        OpsDoc doc0 = doclist.getStmtDocs().get(0);
        List<OpTemplate> ops1 = doc0.getBlocks().get(0).getOps();
        Assertions.assertThat(ops1).hasSize(2);

        OpsBlock block0 = doc0.getBlocks().get(0);
        assertThat(block0.getBindings()).containsExactly(MapEntry.entry("b2","b2d"),MapEntry.entry("b1","b1d"));
        assertThat(block0.getParams()).containsExactly(MapEntry.entry("param1","value1"));
        assertThat(block0.getTags()).containsExactly(MapEntry.entry("atagname","atagvalue"));

        assertThat(ops1.get(0).getBindings()).containsExactly(MapEntry.entry("b2","b2d"),MapEntry.entry("b1","b1d"));
        assertThat(ops1.get(0).getParams()).containsExactly(MapEntry.entry("param1","value1"));
        assertThat(ops1.get(0).getTags()).isEqualTo(Map.of("atagname","atagvalue","name","doc1--block0--stmt1","block","doc1--block0"));

        assertThat(ops1.get(1).getBindings()).containsExactly(MapEntry.entry("b2","b2d"),MapEntry.entry("b1","b1d"));
        assertThat(ops1.get(1).getParams()).containsExactly(MapEntry.entry("param1","value1"));
        assertThat(ops1.get(1).getTags()).isEqualTo(Map.of("atagname","atagvalue","name","doc1--block0--stmt2","block","doc1--block0"));

    }

    @Test
    public void testBlockLayersDocData() {
        OpsDoc doc1 = doclist.getStmtDocs().get(1);
        OpsBlock block0 = doc1.getBlocks().get(0);

        Map<String, String> doc1block0tags = block0.getTags();
        Map<String, String> doc1block0params = block0.getParamsAsText();
        Map<String, String> doc1block0bindings = block0.getBindings();

        assertThat(doc1block0tags).hasSize(3);
        assertThat(doc1block0tags).containsOnly(
                MapEntry.entry("root1","val1"),
                MapEntry.entry("root2","val2"),
                MapEntry.entry("block1tag","tag-value1"));
        assertThat(doc1block0params).containsOnly(
                MapEntry.entry("foobar","baz"),
                MapEntry.entry("timeout","23423"));
        assertThat(doc1block0bindings).containsOnly(
                MapEntry.entry("b12","b12d"),
                MapEntry.entry("b11","override-b11"),
                MapEntry.entry("foobar","overized-beez"));
    }

    @Test
    public void testStmtsGetter() {
        OpsDoc doc1 = doclist.getStmtDocs().get(1);
        List<OpTemplate> stmts = doc1.getOpTemplates();
        Assertions.assertThat(stmts).hasSize(4);
    }

    @Test
    public void testFilteredStmts() {
        List<OpTemplate> stmts = doclist.getOps("");
        Assertions.assertThat(stmts).hasSize(6);
        stmts = doclist.getOps("root1:value23");
        Assertions.assertThat(stmts).hasSize(2);
    }

}

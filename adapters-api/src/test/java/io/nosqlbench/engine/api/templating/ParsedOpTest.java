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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpData;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedOpTest {

    ParsedOp pc = new ParsedOp(
        new OpData().applyFields(
            Map.of(
                "op", Map.of(
                    "stmt", "test",
                    "dyna1", "{dyna1}",
                    "dyna2", "{{NumberNameToString()}}",
                    "identity", "{{Identity()}}"
                ),
                "bindings", Map.of(
                    "dyna1", "NumberNameToString()"
                )
            )
        ),
        ConfigModel.of(ParsedOpTest.class)
            .add(Param.defaultTo("testcfg", "testval"))
            .asReadOnly()
            .apply(Map.of())
    );

    @Test
    public void testFieldDelegationFromDynamicToStaticToConfig() {
        NBConfiguration cfg = ConfigModel.of(ParsedOpTest.class)
            .add(Param.defaultTo("puppy", "dog"))
            .add(Param.required("surname", String.class))
            .asReadOnly().apply(Map.of("surname", "yes"));

        String opt = """
            ops:
               op1:
                 d1: "{{NumberNameToString()}}"
                 s1: "static-one"
                 params:
                  ps1: "param-one"
            """;
        OpsDocList stmtsDocs = OpsLoader.loadString(opt, cfg.getMap());
        assertThat(stmtsDocs.getStmts().size()).isEqualTo(1);
        OpTemplate opTemplate = stmtsDocs.getStmts().get(0);
        ParsedOp parsedOp = new ParsedOp(opTemplate, cfg);

        assertThat(parsedOp.getAsFunctionOr("d1","invalid").apply(1L)).isEqualTo("one");
        assertThat(parsedOp.getAsFunctionOr("s1","invalid").apply(1L)).isEqualTo("static-one");
        assertThat(parsedOp.getAsFunctionOr("ps1","invalid").apply(1L)).isEqualTo("param-one");
        assertThat(parsedOp.getAsFunctionOr("puppy","invalid").apply(1L)).isEqualTo("dog");
        assertThat(parsedOp.getAsFunctionOr("surname","invalid").apply(1L)).isEqualTo("yes");

    }

    @Test
    public void testStaticParamsPromoteToDynamicParams() {

    }


    @Test
    public void testSubMapTemplates() {
        ParsedOp parsedOp = new ParsedOp(
            new OpData().applyFields(Map.of(
                "op", Map.of(
                    "field1-literal", "literalvalue1",
                    "field2-object", "{{NumberNameToString()}}",
                    "field3-template", "pre-{dyna1}-post",
                    "field4-map-template", Map.of(
                        "subfield1-object", "{{Identity(); ToString()}}"
                    ),"field5-map-literal", Map.of(
                        "subfield2-literal", "LiteralValue"
                    )
                ),
                "bindings", Map.of(
                    "dyna1", "NumberNameToString()"
                ))
            ),
            ConfigModel.of(ParsedOpTest.class)
                .add(Param.defaultTo("testcfg", "testval"))
                .asReadOnly()
                .apply(Map.of())
        );
        LongFunction<? extends String> f1 = parsedOp.getAsRequiredFunction("field1-literal");
        LongFunction<? extends String> f2 = parsedOp.getAsRequiredFunction("field2-object");
        LongFunction<? extends String> f3 = parsedOp.getAsRequiredFunction("field3-template");
        LongFunction<? extends Map> f4 = parsedOp.getAsRequiredFunction("field4-map-template",Map.class);
        LongFunction<? extends Map> f5 = parsedOp.getAsRequiredFunction("field5-map-literal",Map.class);
        assertThat(f1.apply(1)).isNotNull();
        assertThat(f2.apply(2)).isNotNull();
        assertThat(f3.apply(3)).isNotNull();
        assertThat(f4.apply(4)).isNotNull();
        assertThat(f5.apply(5)).isNotNull();

    }

    @Test
    public void testParsedOp() {
        Map<String, Object> m1 = pc.apply(0);
        assertThat(m1).containsEntry("stmt", "test");
        assertThat(m1).containsEntry("dyna1", "zero");
        assertThat(m1).containsEntry("dyna2", "zero");
        assertThat(m1).containsEntry("identity", 0L);
    }

    @Test
    public void testNewListBinder() {
        LongFunction<List<Object>> lb = pc.newListBinder("dyna1", "identity", "dyna2", "identity");
        List<Object> objects = lb.apply(1);
        assertThat(objects).isEqualTo(List.of("one", 1L, "one", 1L));
    }

    @Test
    public void testNewMapBinder() {
        LongFunction<Map<String, Object>> mb = pc.newOrderedMapBinder("dyna1", "identity", "dyna2");
        Map<String, Object> objects = mb.apply(2);
        assertThat(objects).isEqualTo(Map.<String, Object>of("dyna1", "two", "identity", 2L, "dyna2", "two"));
    }

    @Test
    public void testNewAryBinder() {
        LongFunction<Object[]> ab = pc.newArrayBinder("dyna1", "dyna1", "identity", "identity");
        Object[] objects = ab.apply(3);
        assertThat(objects).isEqualTo(new Object[]{"three", "three", 3L, 3L});
    }


}

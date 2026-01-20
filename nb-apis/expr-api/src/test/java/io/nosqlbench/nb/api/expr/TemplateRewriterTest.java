/*
 * Copyright (c) 2025 nosqlbench
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

package io.nosqlbench.nb.api.expr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemplateRewriter}, verifying TEMPLATE syntax rewriting to expr function calls.
 */
@Tag("unit")
class TemplateRewriterTest {

    // ==================== TEMPLATE Function Syntax Tests ====================

    @Test
    void testTemplateFunctionWithDefault() {
        String input = "value: TEMPLATE(key,defaultValue)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'defaultValue') }}", output);
    }

    @Test
    void testTemplateFunctionWithoutDefault() {
        String input = "value: TEMPLATE(key)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'UNSET:key') }}", output);
    }

    @Test
    void testTemplateFunctionWithNumericDefault() {
        String input = "count: TEMPLATE(num,1000)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("count: {{= paramOr('num', 1000) }}", output);
    }

    @Test
    void testTemplateFunctionWithFloatDefault() {
        String input = "ratio: TEMPLATE(rate,0.5)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("ratio: {{= paramOr('rate', 0.5) }}", output);
    }

    @Test
    void testTemplateFunctionWithBooleanDefault() {
        String input = "enabled: TEMPLATE(flag,true)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("enabled: {{= paramOr('flag', true) }}", output);
    }

    @Test
    void testTemplateFunctionWithExprDefault() {
        String input = "bind: TEMPLATE(keydist,Uniform(0,1000000000))";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("bind: {{= paramOr('keydist', Uniform(0,1000000000)) }}", output);
    }

    @Test
    void testTemplateFunctionInBindingContext() {
        String input = "rw_key: TEMPLATE(keydist,Uniform(0,1000000000)); ToString()";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("rw_key: {{= paramOr('keydist', Uniform(0,1000000000)) }}; ToString()", output);
    }

    @Test
    void testTemplateFunctionMultipleInSameLine() {
        String input = "TEMPLATE(prefix,test)-TEMPLATE(suffix,end)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("{{= paramOr('prefix', 'test') }}-{{= paramOr('suffix', 'end') }}", output);
    }

    @Test
    void testTemplateFunctionWithEmptyDefault() {
        String input = "value: TEMPLATE(key,)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', null) }}", output);
    }

    @Test
    void testTemplateFunctionComplexExpressionAsDefault() {
        String input = "seq_key: TEMPLATE(keyCount,Mod(1000000); ToString())";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("seq_key: {{= paramOr('keyCount', Mod(1000000); ToString()) }}", output);
    }

    @Test
    void testTemplateFunctionWithinYamlString() {
        String input = "collection: \"TEMPLATE(collection,keyvalue)\"";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("collection: \"{{= paramOr('collection', 'keyvalue') }}\"", output);
    }

    @Test
    void testTemplateFunctionValueWithSingleQuotes() {
        String input = "text: TEMPLATE(msg,it's working)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("text: {{= paramOr('msg', 'it\\'s working') }}", output);
    }

    // ==================== Shell Variable Syntax Tests ====================

    @Test
    void testShellVarWithDefault() {
        String input = "value: ${key:default}";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'default') }}", output);
    }

    @Test
    void testShellVarWithoutDefault() {
        String input = "value: ${key}";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'UNSET:key') }}", output);
    }

    @Test
    void testShellVarWithNumericDefault() {
        String input = "port: ${port:8080}";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("port: {{= paramOr('port', 8080) }}", output);
    }

    // ==================== Mixed Syntax Tests ====================

    @Test
    void testMixedTemplateFunctionAndShellVar() {
        String input = "TEMPLATE(prefix,test)-${middle:value}-TEMPLATE(suffix,end)";
        String output = TemplateRewriter.rewrite(input);

        String expected = "{{= paramOr('prefix', 'test') }}-{{= paramOr('middle', 'value') }}-{{= paramOr('suffix', 'end') }}";
        assertEquals(expected, output);
    }

    @Test
    void testMixedSyntaxAcrossMultipleLines() {
        String input = """
            prefix: TEMPLATE(start,begin)
            middle: TEMPLATE(center,mid)
            suffix: ${end:finish}
            """;
        String output = TemplateRewriter.rewrite(input);

        assertTrue(output.contains("{{= paramOr('start', 'begin') }}"));
        assertTrue(output.contains("{{= paramOr('center', 'mid') }}"));
        assertTrue(output.contains("{{= paramOr('end', 'finish') }}"));
    }

    // ==================== Edge Cases ====================

    @Test
    void testEmptyString() {
        String input = "";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("", output);
    }

    @Test
    void testNullString() {
        String input = null;
        String output = TemplateRewriter.rewrite(input);

        assertEquals(null, output);
    }

    @Test
    void testNoTemplateVariables() {
        String input = "simple: value without templates";
        String output = TemplateRewriter.rewrite(input);

        assertEquals(input, output);
    }

    // ==================== Real-World Examples ====================

    @Test
    void testBindingExample() {
        String input = "seq_key: Mod(TEMPLATE(keyCount,1000000)); ToString();";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("seq_key: Mod({{= paramOr('keyCount', 1000000) }}); ToString();", output);
    }

    @Test
    void testScenarioExample() {
        String input = "rampup: run cycles===TEMPLATE(rampup-cycles,10)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("rampup: run cycles==={{= paramOr('rampup-cycles', 10) }}", output);
    }

    // ==================== Nested Expression References ====================

    @Test
    void testTemplateFunctionWithNestedExprReference() {
        String input = "index_cycles: TEMPLATE(index_cycles,{{=base_vectors}})";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("index_cycles: {{= paramOr('index_cycles', base_vectors) }}", output);
    }

    @Test
    void testTemplateFunctionWithNestedExprReferenceWithSpaces() {
        String input = "count: TEMPLATE(count,{{= default_count }})";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("count: {{= paramOr('count', default_count) }}", output);
    }

    @Test
    void testTemplateFunctionWithNestedExprFunctionCall() {
        String input = "value: TEMPLATE(val,{{= paramOr('x', 100) }})";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('val', paramOr('x', 100)) }}", output);
    }

    @Test
    void testShellVarWithNestedExprReference() {
        String input = "cycles: ${cycles:{{=base_cycles}}}";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("cycles: {{= paramOr('cycles', base_cycles) }}", output);
    }

    @Test
    void testTemplateFunctionWithMultipleNestedExprs() {
        String input = "sum: TEMPLATE(sum,{{=x}} + {{=y}})";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("sum: {{= paramOr('sum', x + y) }}", output);
    }

    @Test
    void testTemplateFunctionWithExprInMiddle() {
        String input = "msg: TEMPLATE(msg,prefix {{=name}} suffix)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("msg: {{= paramOr('msg', 'prefix name suffix') }}", output);
    }

    @Test
    void testShellVarWithMultipleNestedExprs() {
        String input = "result: ${result:{{=a}} * {{=b}}}";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("result: {{= paramOr('result', a * b) }}", output);
    }

    // ==================== Preserve Non-Template Content ====================

    @Test
    void testPreserveYamlStructure() {
        String input = """
            bindings:
              key: Mod(TEMPLATE(count,1000))
            ops:
              stmt: "value-TEMPLATE(id,123)"
            """;
        String output = TemplateRewriter.rewrite(input);

        // YAML structure should be preserved
        assertTrue(output.contains("bindings:"));
        assertTrue(output.contains("ops:"));
        // Templates should be rewritten
        assertTrue(output.contains("{{= paramOr('count', 1000) }}"));
        assertTrue(output.contains("{{= paramOr('id', 123) }}"));
    }

    @Test
    void testPreserveExistingExpressions() {
        String input = "existing: {{= 1 + 2 }} and template: TEMPLATE(key,value)";
        String output = TemplateRewriter.rewrite(input);

        // Existing expressions should be untouched
        assertTrue(output.contains("{{= 1 + 2 }}"));
        // Template should be rewritten
        assertTrue(output.contains("{{= paramOr('key', 'value') }}"));
    }
}

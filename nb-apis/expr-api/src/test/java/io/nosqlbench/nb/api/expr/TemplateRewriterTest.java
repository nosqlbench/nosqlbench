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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemplateRewriter}, verifying TEMPLATE syntax rewriting to expr function calls.
 */
class TemplateRewriterTest {

    // ==================== Angle Bracket Syntax Tests ====================

    @Test
    void testAngleBracketSimpleDefault() {
        String input = "value: <<key:default>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'default') }}", output);
    }

    @Test
    void testAngleBracketNoDefault() {
        String input = "value: <<key>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'UNSET:key') }}", output);
    }

    @Test
    void testAngleBracketNumericDefault() {
        String input = "count: <<num:100>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("count: {{= paramOr('num', 100) }}", output);
    }

    @Test
    void testAngleBracketFloatDefault() {
        String input = "ratio: <<rate:0.5>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("ratio: {{= paramOr('rate', 0.5) }}", output);
    }

    @Test
    void testAngleBracketBooleanDefault() {
        String input = "enabled: <<flag:true>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("enabled: {{= paramOr('flag', true) }}", output);
    }

    @Test
    void testAngleBracketWithExprDefault() {
        String input = "bind: <<dist:Uniform(0,1000)>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("bind: {{= paramOr('dist', Uniform(0,1000)) }}", output);
    }

    @Test
    void testAngleBracketMultipleInSameLine() {
        String input = "<<prefix:test>>-<<suffix:end>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("{{= paramOr('prefix', 'test') }}-{{= paramOr('suffix', 'end') }}", output);
    }

    // ==================== Operator Mode Tests ====================

    @Test
    void testAngleBracketRequiredOperator() {
        String input = "required: <<apikey:?>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("required: {{= param('apikey') }}", output);
    }

    @Test
    void testAngleBracketSetOperator() {
        String input = "retries: <<count:=3>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("retries: {{= _templateSet('count', 3) }}", output);
    }

    @Test
    void testAngleBracketAlternateOperator() {
        String input = "mode: <<debug:+verbose>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("mode: {{= _templateAlt('debug', 'verbose') }}", output);
    }

    @Test
    void testAngleBracketExplicitDefaultOperator() {
        String input = "port: <<port:-8080>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("port: {{= paramOr('port', 8080) }}", output);
    }

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
    void testMixedSyntaxInSingleString() {
        String input = "<<prefix:test>>-TEMPLATE(middle,value)-${suffix:end}";
        String output = TemplateRewriter.rewrite(input);

        String expected = "{{= paramOr('prefix', 'test') }}-{{= paramOr('middle', 'value') }}-{{= paramOr('suffix', 'end') }}";
        assertEquals(expected, output);
    }

    @Test
    void testMixedSyntaxAcrossMultipleLines() {
        String input = """
            prefix: <<start:begin>>
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

    @Test
    void testWhitespaceHandling() {
        String input = "value: <<  key  :  default  >>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("value: {{= paramOr('key', 'default') }}", output);
    }

    @Test
    void testValueWithSingleQuotes() {
        String input = "text: <<msg:it's working>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("text: {{= paramOr('msg', 'it\\'s working') }}", output);
    }

    // ==================== Complex/Nested Value Tests ====================

    @Test
    void testComplexExpressionAsDefault() {
        String input = "seq_key: <<keyCount:Mod(1000000); ToString()>>";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("seq_key: {{= paramOr('keyCount', Mod(1000000); ToString()) }}", output);
    }

    @Test
    void testNestedAngleBrackets() {
        // This tests whether the rewriter handles templates inside templates
        // After first pass, inner template becomes an expr, which outer template should preserve
        String input = "outer: <<a:<<b:inner>>>>";
        String output = TemplateRewriter.rewrite(input);

        // After first rewrite, inner <<b:inner>> becomes {{= paramOr('b', 'inner') }}
        // Then outer <<a:...>> should wrap that
        assertNotNull(output);
        assertTrue(output.contains("paramOr"));
    }

    @Test
    void testTemplateWithinYamlString() {
        String input = "collection: \"<<collection:keyvalue>>\"";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("collection: \"{{= paramOr('collection', 'keyvalue') }}\"", output);
    }

    // ==================== Real-World Examples from Blueprint ====================

    @Test
    void testBindingExample() {
        String input = "seq_key: Mod(<<keyCount:1000000>>); ToString();";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("seq_key: Mod({{= paramOr('keyCount', 1000000) }}); ToString();", output);
    }

    @Test
    void testScenarioExample() {
        String input = "rampup: run cycles===TEMPLATE(rampup-cycles,10)";
        String output = TemplateRewriter.rewrite(input);

        assertEquals("rampup: run cycles==={{= paramOr('rampup-cycles', 10) }}", output);
    }

    @Test
    void testMultiOperatorExample() {
        String input = """
            default: <<port:-8080>>
            set: <<retries:=3>>
            required: <<apikey:?>>
            alternate: <<debug:+verbose>>
            """;
        String output = TemplateRewriter.rewrite(input);

        assertTrue(output.contains("{{= paramOr('port', 8080) }}"));
        assertTrue(output.contains("{{= _templateSet('retries', 3) }}"));
        assertTrue(output.contains("{{= param('apikey') }}"));
        assertTrue(output.contains("{{= _templateAlt('debug', 'verbose') }}"));
    }

    // ==================== Preserve Non-Template Content ====================

    @Test
    void testPreserveYamlStructure() {
        String input = """
            bindings:
              key: Mod(<<count:1000>>)
            ops:
              stmt: "value-<<id:123>>"
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
        String input = "existing: {{= 1 + 2 }} and template: <<key:value>>";
        String output = TemplateRewriter.rewrite(input);

        // Existing expressions should be untouched
        assertTrue(output.contains("{{= 1 + 2 }}"));
        // Template should be rewritten
        assertTrue(output.contains("{{= paramOr('key', 'value') }}"));
    }
}

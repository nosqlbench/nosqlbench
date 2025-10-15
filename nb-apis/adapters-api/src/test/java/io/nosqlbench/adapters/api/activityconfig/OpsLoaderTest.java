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

package io.nosqlbench.adapters.api.activityconfig;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDoc;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpsLoaderTest {

    @Test
    public void testTemplateVarSubstitution() {
        OpsDocList opsDocs = OpsLoader.loadPath("activities/template_vars", new HashMap<>(),"src/test/resources");
        assertThat(opsDocs).isNotNull();
        List<OpsDoc> docs = opsDocs.getStmtDocs();
        assertThat(docs).hasSize(1);
        OpsDoc opsDoc = docs.get(0);
        Map<String, String> bindings = opsDoc.getBindings();
        assertThat(bindings).isEqualTo(Map.of(
            "b1a","Prefix(\"prefix\")",
            "b1b","Prefix(\"prefix\")",
            "b2a","Suffix(\"suffix\")",
            "b2b","Suffix(\"suffix\")"));
    }

    @Test
    public void testInvalidYamlProperties() {
        Exception caught = null;
        try {
            OpsLoader.loadPath("activities/invalid_prop", Map.of(),"src/test/resources");
        } catch (Exception e) {
            caught = e;
        }
        assertThat(caught).isNotNull();
    }

    @Test
    public void testTemplateRewriterIntegration_AngleBracketSyntax() {
        // Test <<key:default>> syntax gets rewritten and processed
        String yaml = """
            bindings:
              key: Mod(<<count:1000>>)
            ops:
              - stmt: "value-<<id:123>>"
            """;

        Map<String, String> params = new HashMap<>();
        params.put("count", "5000");

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        // Verify the template was resolved via expr system
        Map<String, String> bindings = docs.get(0).getBindings();
        assertThat(bindings.get("key")).isEqualTo("Mod(5000)");

        // Verify the statement has the default value
        List<OpTemplate> templates = docs.get(0).getOpTemplates();
        assertThat(templates.get(0).getStmt()).hasValue("value-123");

        // Verify template variable was tracked
        assertThat(result.getTemplateVariables()).containsKey("count");
    }

    @Test
    public void testTemplateRewriterIntegration_TemplateFunctionSyntax() {
        // Test TEMPLATE(key,default) syntax
        String yaml = """
            bindings:
              name: TEMPLATE(username,defaultUser)
            ops:
              - stmt: "query-TEMPLATE(querytype,select)"
            """;

        Map<String, String> params = new HashMap<>();
        params.put("username", "testUser");

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        // Verify parameter override worked
        Map<String, String> bindings = docs.get(0).getBindings();
        assertThat(bindings.get("name")).isEqualTo("testUser");

        // Verify default value was used
        List<OpTemplate> templates = docs.get(0).getOpTemplates();
        assertThat(templates.get(0).getStmt()).hasValue("query-select");

        // Verify template tracking
        assertThat(result.getTemplateVariables()).containsKey("username");
    }

    @Test
    public void testTemplateRewriterIntegration_ShellVarSyntax() {
        // Test ${key:default} syntax
        String yaml = """
            bindings:
              port: "${server_port:8080}"
            ops:
              - stmt: "connect-${protocol:http}"
            """;

        Map<String, String> params = new HashMap<>();
        params.put("server_port", "9090");

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        // Verify parameter override
        Map<String, String> bindings = docs.get(0).getBindings();
        assertThat(bindings.get("port")).isEqualTo("9090");

        // Verify default value
        List<OpTemplate> templates = docs.get(0).getOpTemplates();
        assertThat(templates.get(0).getStmt()).hasValue("connect-http");
    }

    @Test
    public void testTemplateRewriterIntegration_RequiredParameter() {
        // Test <<key:?>> syntax (required parameter)
        String yaml = """
            ops:
              - stmt: "apikey-<<apikey:?>>"
            """;

        Map<String, String> params = new HashMap<>();

        // Should throw because apikey is required but not provided
        Exception caught = null;
        try {
            OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);
        } catch (Exception e) {
            caught = e;
        }

        assertThat(caught).isNotNull();
        assertThat(caught.getMessage()).contains("apikey");
    }

    @Test
    public void testTemplateRewriterIntegration_SetOperator() {
        // Test <<key:=default>> syntax (:= operator)
        String yaml = """
            bindings:
              retries: "<<retry_count:=3>>"
            ops:
              - stmt: "attempt-<<retry_count:=3>>"
            """;

        Map<String, String> params = new HashMap<>();

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        // Both should use the same default value
        Map<String, String> bindings = docs.get(0).getBindings();
        assertThat(bindings.get("retries")).isEqualTo("3");
        List<OpTemplate> templates = docs.get(0).getOpTemplates();
        assertThat(templates.get(0).getStmt()).hasValue("attempt-3");
    }

    @Test
    public void testTemplateRewriterIntegration_AlternateOperator() {
        // Test <<key:+alternate>> syntax (:+ operator)
        String yaml = """
            ops:
              - stmt: "mode-<<debug:+verbose>>"
            """;

        Map<String, String> params = new HashMap<>();
        params.put("debug", "true");

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        // Should use alternate value since debug is set
        List<OpTemplate> templates = docs.get(0).getOpTemplates();
        assertThat(templates.get(0).getStmt()).hasValue("mode-verbose");
    }

    @Test
    public void testTemplateRewriterIntegration_MixedSyntax() {
        // Test all three syntaxes in one workload
        String yaml = """
            bindings:
              angle: <<count:100>>
              template: TEMPLATE(username,defaultUser)
              shell: "${port:8080}"
            ops:
              - stmt: "query"
            """;

        Map<String, String> params = new HashMap<>();
        params.put("count", "200");
        params.put("username", "testUser");

        OpsDocList result = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, params, null);

        assertThat(result).isNotNull();
        List<OpsDoc> docs = result.getStmtDocs();
        assertThat(docs).hasSize(1);

        Map<String, String> bindings = docs.get(0).getBindings();
        assertThat(bindings.get("angle")).isEqualTo("200");
        assertThat(bindings.get("template")).isEqualTo("testUser");
        assertThat(bindings.get("shell")).isEqualTo("8080");

        // Verify both parameters were tracked
        assertThat(result.getTemplateVariables()).containsKeys("count", "username");
    }

}

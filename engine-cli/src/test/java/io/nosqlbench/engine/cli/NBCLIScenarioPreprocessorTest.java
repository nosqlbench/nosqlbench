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

package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.scenarios.NBCLIScenarioPreprocessor;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NBCLIScenarioPreprocessorTest {

    @Test
    public void providePathForScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"local/example_scenarios"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void defaultScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void defaultScenarioWithParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "cycles=100"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArgValue("cycles")).isEqualTo("100");
    }

    @Test
    public void namedScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "schema_only"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void namedScenarioWithParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "schema_only", "cycles=100"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArgValue("cycles")).containsOnlyOnce("100");
    }

    @Test
    public void testThatSilentFinalParametersPersist() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "type=foo"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArgValue("driver")).isEqualTo("stdout");
    }

    @Test
    public void testThatVerboseFinalParameterThrowsError() {
        assertThatExceptionOfType(BasicError.class)
            .isThrownBy(() -> new NBCLIOptions(new String[]{"scenario_test", "workload=canttouchthis"}, NBCLIOptions.Mode.ParseAllOptions));
    }

    @Test
    public void testThatMissingScenarioNameThrowsError() {
        assertThatExceptionOfType(BasicError.class)
            .isThrownBy(() -> new NBCLIOptions(new String[]{"scenario_test", "missing_scenario"}, NBCLIOptions.Mode.ParseAllOptions));
    }

    @Test
    public void testThatMultipleScenariosConcatenate() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "default", "default"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(6);
    }

    @Test
    public void testThatTemplatesAreExpandedDefault() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "template_test"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArgValue("driver")).isEqualTo("stdout");
        assertThat(cmds.get(0).getArgValue("cycles")).isEqualTo("10");
        assertThat(cmds.get(0).getArgValue("workload")).isEqualTo("scenario_test");
    }

    @Test
    public void testThatTemplateParamsAreExpandedAndNotRemovedOverride() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "template_test", "cycles-test=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArgMap()).isEqualTo(Map.of(
            "_impl","run",
            "alias", "with_template",
            "container", "template_test",
            "cycles", "20",
            "cycles-test", "20",
            "driver", "stdout",
            "labels", "workload:scenario_test,scenario:template_test",
            "step", "with_template",
            "workload", "scenario_test"
        ));
    }

    @Test
    public void testThatUndefValuesAreUndefined() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "schema_only", "cycles-test=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArgMap()).isEqualTo(Map.of(
            "_impl", "run",
            "alias", "schema",
            "container", "schema_only",
            "cycles-test", "20",
            "driver", "stdout",
            "labels", "workload:scenario_test,scenario:schema_only",
            "step", "schema",
            "tags", "block:\"schema.*\"",
            "workload", "scenario_test"
        ));
        NBCLIOptions opts1 = new NBCLIOptions(new String[]{"scenario_test", "schema_only", "doundef=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds1 = opts1.getCommands();
        assertThat(cmds1.size()).isEqualTo(1);
        assertThat(cmds1.get(0).getArgValueOrNull("cycles-test")).isNull();
    }

    @Test
    public void testThatFullyQualifiedScenarioFilesAreSupported() {
        Path cwd = Path.of(".").toAbsolutePath();
        System.out.println("cwd: '" + cwd + "'");

        Path rel = Path.of("src/test/resources/activities/scenario_test.yaml");

        assertThat(rel).exists();
        Path absolute = rel.toAbsolutePath();
        assertThat(absolute).exists();

        NBCLIOptions opts = new NBCLIOptions(new String[]{absolute.toString(), "schema_only", "cycles-test=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isGreaterThan(0);
    }

    @Disabled
    @Test
    public void testThatScenarioUrlsAreSupported() {
        //TODO: This might change?
        String urlScenario = "https://raw.githubusercontent.com/nosqlbench/nosqlbench/main/engine-cli/src/test/resources/activities/scenario_test.yaml";

        NBCLIOptions opts = new NBCLIOptions(new String[]{urlScenario, "schema_only", "cycles-test=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isGreaterThan(0);
    }

    @Test
    public void testSanitizer() {
        String sanitized = NBCLIScenarioPreprocessor.sanitize("A-b,c_d");
        assertThat(sanitized).isEqualTo("A_bc_d");
    }

    @Test
    public void testSubStepSelection() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"scenario_test", "schema_only", "cycles-test=20"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArgMap()).isEqualTo(Map.of(
            "_impl","run",
            "alias", "schema",
            "container", "schema_only",
            "cycles-test", "20",
            "driver", "stdout",
            "labels", "workload:scenario_test,scenario:schema_only",
            "step", "schema",
            "tags", "block:\"schema.*\"",
            "workload", "scenario_test"
        ));
        NBCLIOptions opts1 = new NBCLIOptions(new String[]{"local/example_scenarios", "namedsteps.one", "testparam1=testvalue2"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds1 = opts1.getCommands();
        assertThat(cmds1.size()).isEqualTo(1);
        assertThat(cmds1.get(0).getArgValueOrNull("cycles_test")).isNull();

    }

    @Test
    public void testThatDuplicateParamInScenarioDefThrowsError() {
        assertThatExceptionOfType(BasicError.class)
            .isThrownBy(() -> new NBCLIOptions(new String[]{"scenario_test", "duplicate_param"}, NBCLIOptions.Mode.ParseAllOptions))
            .withMessageContaining("Duplicate occurrence of parameter \"threads\"");
    }
}

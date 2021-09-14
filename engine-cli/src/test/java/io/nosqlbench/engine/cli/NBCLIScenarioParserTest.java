package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NBCLIScenarioParserTest {

    @Test
    public void providePathForScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios" });
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void defaultScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test" });
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void defaultScenarioWithParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "cycles=100"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArg("cycles")).isEqualTo("100");
    }

    @Test
    public void namedScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "schema-only"});
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void namedScenarioWithParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "schema-only", "cycles=100"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArg("cycles")).containsOnlyOnce("100");
    }

    @Test
    public void testThatSilentFinalParametersPersist() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "type=foo"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getArg("driver")).isEqualTo("stdout");
    }

    @Test
    public void testThatVerboseFinalParameterThrowsError() {
        assertThatExceptionOfType(BasicError.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "scenario-test", "yaml=canttouchthis"}));
    }

    @Test
    public void testThatMissingScenarioNameThrowsError() {
        assertThatExceptionOfType(BasicError.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "scenario-test", "missing-scenario"}));
    }

    @Test
    public void testThatMultipleScenariosConcatenate() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "default", "default"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(6);
    }

    @Test
    public void testThatTemplatesAreExpandedDefault() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "template-test"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArg("driver")).isEqualTo("stdout");
        assertThat(cmds.get(0).getArg("cycles")).isEqualTo("10");
        assertThat(cmds.get(0).getArg("workload")).isEqualTo("target/test-classes/activities/scenario-test.yaml");
    }

    @Test
    public void testThatTemplatesAreExpandedOverride() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "template-test", "cycles-test=20"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArg("driver")).isEqualTo("stdout");
        assertThat(cmds.get(0).getArg("cycles")).isEqualTo("20");
        assertThat(cmds.get(0).getArg("cycles-test")).isEqualTo("20");
        assertThat(cmds.get(0).getArg("workload")).isEqualTo("target/test-classes/activities/scenario-test.yaml");
    }

    @Test
    public void testThatUndefValuesAreUndefined() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "scenario-test", "schema-only", "cycles-test=20"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getArg("cycles-test")).isEqualTo("20");
        NBCLIOptions opts1 = new NBCLIOptions(new String[]{ "scenario-test", "schema-only", "doundef=20"});
        List<Cmd> cmds1 = opts1.getCommands();
        assertThat(cmds1.size()).isEqualTo(1);
        assertThat(cmds1.get(0).getArg("cycles-test")).isNull();

    }


    @Test
    public void testSanitizer() {
        String sanitized = NBCLIScenarioParser.sanitize("A-b,c_d");
        assertThat(sanitized).isEqualTo("Abcd");

    }
}

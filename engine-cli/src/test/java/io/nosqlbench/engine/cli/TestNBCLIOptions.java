package io.nosqlbench.engine.cli;

import io.nosqlbench.docsys.core.PathWalker;
import io.nosqlbench.nb.api.content.NBIO;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNBCLIOptions {

    @Test
    public void shouldRecognizeActivities() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "foo=wan", "start", "bar=lan"});
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
        assertThat(opts.getCommands().get(0).getParams()).containsEntry("foo","wan");
        assertThat(opts.getCommands().get(1).getParams()).containsEntry("bar","lan");
    }

    @Test
    public void shouldParseLongActivityForm() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "param1=param2", "param3=param4",
                                                          "--report-graphite-to", "woot", "--report-interval", "23"});
        assertThat(opts.getCommands().size()).isEqualTo(1);
        assertThat(opts.getCommands().get(0).getParams()).containsEntry("param1","param2");
        assertThat(opts.getCommands().get(0).getParams()).containsEntry("param3","param4");
        assertThat(opts.wantsReportGraphiteTo()).isEqualTo("woot");
        assertThat(opts.getReportInterval()).isEqualTo(23);
    }

    @Test
    public void shouldRecognizeShortVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.isWantsVersionShort()).isTrue();
        assertThat(opts.wantsVersionCoords()).isFalse();
    }

    @Test
    public void shouldRecognizeVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version-coords"});
        assertThat(opts.isWantsVersionShort()).isFalse();
        assertThat(opts.wantsVersionCoords()).isTrue();
    }

    @Test
    public void shouldRecognizeScripts() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "ascriptaone", "script", "ascriptatwo"});
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
        assertThat(opts.getCommands().get(0).getCmdType()).isEqualTo(Cmd.CmdType.script);
        assertThat(opts.getCommands().get(0).getArg("script_path")).isEqualTo("ascriptaone");
        assertThat(opts.getCommands().get(1).getCmdType()).isEqualTo(Cmd.CmdType.script);
        assertThat(opts.getCommands().get(1).getArg("script_path")).isEqualTo("ascriptatwo");
    }

    @Test
    public void shouldRecognizeWantsActivityTypes() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--list-activity-types"});
        assertThat(opts.wantsActivityTypes()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsActivityTypes()).isFalse();
        opts = new NBCLIOptions(new String[]{"--list-drivers"});
        assertThat(opts.wantsActivityTypes()).isTrue();

    }

    @Test
    public void shouldRecognizeWantsBasicHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help"});
        assertThat(opts.wantsBasicHelp()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test
    public void shouldRecognizeWantsActivityHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help", "foo"});
        assertThat(opts.wantsTopicalHelp()).isTrue();
        assertThat(opts.wantsTopicalHelpFor()).isEqualTo("foo");
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorSanelyWhenNoMatch() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"unrecognizable command"});
    }

    @Test
    public void testShouldRecognizeScriptParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "ascript", "param1=value1"});
        assertThat(opts.getCommands().size()).isEqualTo(1);
        Cmd cmd = opts.getCommands().get(0);
        assertThat(cmd.getParams().size()).isEqualTo(2);
        assertThat(cmd.getParams()).containsKey("param1");
        assertThat(cmd.getParams().get("param1")).isEqualTo("value1");
    }

    @Test(expected = InvalidParameterException.class)
    public void testShouldErrorSanelyWhenScriptNameSkipped() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "param1=value1"});
    }

    @Test(expected = InvalidParameterException.class)
    public void testShouldErrorForMissingScriptName() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script"});
    }

    @Test
    public void shouldRecognizeStartActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "start", "driver=woot" });
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.start);

    }

    @Test
    public void shouldRecognizeRunActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "run", "driver=runwoot" });
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.run);

    }

    @Test
    public void shouldRecognizeStopActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "stop", "woah" });
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.stop);
        assertThat(cmds.get(0).getArg("alias_name")).isEqualTo("woah");

    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowErrorForInvalidStopActivity() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "stop", "woah=woah" });
        List<Cmd> cmds = opts.getCommands();
    }

    @Test
    public void shouldRecognizeAwaitActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "await", "awaitme" });
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.await);
        assertThat(cmds.get(0).getArg("alias_name")).isEqualTo("awaitme");

    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowErrorForInvalidAwaitActivity() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "await", "awaitme=notvalid" });
        List<Cmd> cmds = opts.getCommands();

    }

    @Test
    public void shouldRecognizewaitMillisCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "waitmillis", "23234" });
        List<Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.waitMillis);
        assertThat(cmds.get(0).getArg("millis_to_wait")).isEqualTo("23234");

    }

    @Test
    public void listWorkloads() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--list-workloads"});
        List<Cmd> cmds = opts.getCommands();
        assertThat(opts.wantsScenariosList());
    }


    @Test
    public void clTest() {
        String dir= "./";
        URL resource = getClass().getClassLoader().getResource(dir);
        assertThat(resource);
        Path basePath = NBIO.getFirstLocalPath(dir);
        List<Path> yamlPathList = PathWalker.findAll(basePath).stream().filter(f -> f.toString().endsWith(".yaml")).collect(Collectors.toList());
        assertThat(yamlPathList);
    }


}

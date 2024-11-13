/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.docsys.core.PathWalker;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdType;
import io.nosqlbench.nb.api.nbio.NBIO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TestNBCLIOptions {

    @Test
    public void shouldRecognizeActivities() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "foo=wan", "start", "bar=lan"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
//        assertThat(opts.getCommands().get(0).getArgs()).containsEntry("foo","wan");
//        assertThat(opts.getCommands().get(1).getArgs()).containsEntry("bar","lan");
    }

    @Test
    public void shouldParseLongActivityForm() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "param1=param2", "param3=param4",
                                                          "--report-graphite-to", "woot", "--report-interval", "23"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.getCommands().size()).isEqualTo(1);
//        assertThat(opts.getCommands().get(0).getArgs()).containsEntry("param1","param2");
//        assertThat(opts.getCommands().get(0).getArgs()).containsEntry("param3","param4");
        assertThat(opts.wantsReportGraphiteTo()).isEqualTo("woot");
        assertThat(opts.getReportInterval()).isEqualTo(23);
    }

    @Test
    public void shouldRecognizeShortVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.isWantsVersionShort()).isTrue();
        assertThat(opts.wantsVersionCoords()).isFalse();
    }

    @Test
    public void shouldRecognizeVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version-coords"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.isWantsVersionShort()).isFalse();
        assertThat(opts.wantsVersionCoords()).isTrue();
    }

    @Test
    public void shouldRecognizeScripts() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "path=ascriptaone", "script", "path=ascriptatwo"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
        assertThat(opts.getCommands().get(0).getCmdType()).isEqualTo(CmdType.script);
        assertThat(opts.getCommands().get(0).getArgValue("path")).isEqualTo("ascriptaone");
        assertThat(opts.getCommands().get(1).getCmdType()).isEqualTo(CmdType.script);
        assertThat(opts.getCommands().get(1).getArgValue("path")).isEqualTo("ascriptatwo");
    }

    @Test
    public void shouldRecognizeWantsActivityTypes() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--list-activity-types"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsActivityTypes()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsActivityTypes()).isFalse();
        opts = new NBCLIOptions(new String[]{"--list-drivers"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsActivityTypes()).isTrue();

    }

    @Test
    public void shouldRecognizeWantsBasicHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsBasicHelp()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test
    public void shouldRecognizeWantsActivityHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help", "foo"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsTopicalHelp()).isTrue();
        assertThat(opts.wantsTopicalHelpFor()).isEqualTo("foo");
        opts = new NBCLIOptions(new String[]{"--version"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test
    public void shouldErrorSanelyWhenNoMatch() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{"unrecognizable command"}, NBCLIOptions.Mode.ParseAllOptions));
    }

    @Test
    public void testShouldRecognizeScriptParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "path=ascript", "param1=value1"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.getCommands().size()).isEqualTo(1);
        Cmd cmd = opts.getCommands().get(0);
        assertThat(cmd.getArgs().size()).isEqualTo(2);
        assertThat(cmd.getArgs()).containsKey("param1");
        assertThat(cmd.getArgValue("param1")).isEqualTo("value1");
    }

    @Disabled("bare positional parameters are no longer supported for commands, only named parameters")
    @Test
    public void testShouldErrorSanelyWhenScriptNameSkipped() {
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{"script", "param1=value1"}, NBCLIOptions.Mode.ParseAllOptions));
    }

    @Disabled("semantic parsing is deferred until later")
    @Test
    public void testShouldErrorForMissingScriptName() {
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{"script"}, NBCLIOptions.Mode.ParseAllOptions));
    }

//    @Test
//    public void shouldRecognizeStartActivityCmd() {
//        NBCLIOptions opts = new NBCLIOptions(new String[]{ "start", "driver=woot" });
//        List<Cmd> cmds = opts.getCommands();
//        assertThat(cmds).hasSize(1);
//        assertThat(cmds.get(0).getCmdType()).isEqualTo(CmdType.start);
//
//    }
//
//    @Test
//    public void shouldRecognizeRunActivityCmd() {
//        NBCLIOptions opts = new NBCLIOptions(new String[]{ "run", "driver=runwoot" });
//        List<Cmd> cmds = opts.getCommands();
//        assertThat(cmds).hasSize(1);
//        assertThat(cmds.get(0).getCmdType()).isEqualTo(CmdType.run);
//
//    }

//    @Test
//    public void shouldRecognizeStopActivityCmd() {
//        NBCLIOptions opts = new NBCLIOptions(new String[]{ "stop", "activity=woah" });
//        List<Cmd> cmds = opts.getCommands();
//        assertThat(cmds).hasSize(1);
//        assertThat(cmds.get(0).getCmdType()).isEqualTo(CmdType.stop);
//        assertThat(cmds.get(0).getArgValue("activity")).isEqualTo("woah");
//
//    }
//
    @Disabled("semantic parsing is deferred until later")
    @Test
    public void shouldThrowErrorForInvalidStopActivity() {
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "stop", "woah=woah" }, NBCLIOptions.Mode.ParseAllOptions));
    }

//    @Test
//    public void shouldRecognizeAwaitActivityCmd() {
//        NBCLIOptions opts = new NBCLIOptions(new String[]{ "await", "activity=awaitme" });
//        List<Cmd> cmds = opts.getCommands();
//        assertThat(cmds.get(0).getCmdType()).isEqualTo(CmdType.await);
//        assertThat(cmds.get(0).getArgValue("activity")).isEqualTo("awaitme");
//
//    }

    @Disabled("semantic parsing is reserved until later after generalizing syntax")
    @Test
    public void shouldThrowErrorForInvalidAwaitActivity() {
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "await", "awaitme=notvalid" }, NBCLIOptions.Mode.ParseAllOptions));
    }

//    @Test
//    public void shouldRecognizewaitMillisCmd() {
//        NBCLIOptions opts = new NBCLIOptions(new String[]{ "waitmillis", "ms=23234" });
//        List<Cmd> cmds = opts.getCommands();
//        assertThat(cmds.get(0).getCmdType()).isEqualTo(CmdType.waitMillis);
//        assertThat(cmds.get(0).getArgValue("ms")).isEqualTo("23234");
//
//    }

    @Test
    public void testLoggerConfigData() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--log-histograms", "console.log:.*:30000"}, NBCLIOptions.Mode.ParseAllOptions);
        for (final NBCLIOptions.LoggerConfigData histoLogger : opts.getHistoLoggerConfigs()) {
            assertThat(histoLogger.pattern).isEqualTo(".*");
            assertThat(histoLogger.file).isEqualTo("console.log");
            assertThat(histoLogger.millis).isEqualTo(30000L);
        }
    }

    @Test
    public void listWorkloads() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--list-workloads"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsWorkloadsList()).isTrue();
    }

    @Test
    public void listScenarios() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--list-scenarios"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsScenariosList()).isTrue();
    }

    @Test
    public void listScripts() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--list-scripts"}, NBCLIOptions.Mode.ParseAllOptions);
        assertThat(opts.wantsListScripts()).isTrue();
    }

    @Test
    public void clTest() {
        String dir= "./";
        URL resource = getClass().getClassLoader().getResource(dir);
        assertThat(resource).isNotNull();
        Path basePath = NBIO.getFirstLocalPath(dir);
        List<Path> yamlPathList = PathWalker.findAll(basePath).stream().filter(f -> f.toString().endsWith(".yaml")).collect(Collectors.toList());
        assertThat(yamlPathList).isNotEmpty();
    }
}

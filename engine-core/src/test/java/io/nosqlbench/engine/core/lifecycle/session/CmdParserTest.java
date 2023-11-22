/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {

    @Test
    public void testSingleCommand() {
        List<Cmd> cmds = CmdParser.parse("testcmd42");
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.indirect);
        assertThat(cmds.get(0).getArg("_name")).isEqualTo("testcmd42");
    }

    @Test
    public void testSingleCommandWithArgs() {
        List<Cmd> cmds = CmdParser.parse("testcmd43 param1=value1");
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.indirect);
        assertThat(cmds.get(0).getArg("_name")).isEqualTo("testcmd43");
        assertThat(cmds.get(0).getArg("param1")).isEqualTo("value1");
    }

    @Test
    public void testSingleDquotedArg() {
        List<Cmd> cmds = CmdParser.parse("testcmd44 param1=\"value1\"");
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.indirect);
        assertThat(cmds.get(0).getArg("_name")).isEqualTo("testcmd44");
        assertThat(cmds.get(0).getArg("param1")).isEqualTo("value1");
    }

    @Test
    public void testSpecialSymbolValue() {
        List<Cmd> cmds = CmdParser.parse("start param1="+ CmdParser.SYMBOLS+ " param2='"+ CmdParser.SYMBOLS+ "' param3=\""+ CmdParser.SYMBOLS+ "\"");
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.start);
        assertThat(cmds.get(0).getArg("param1")).isEqualTo(CmdParser.SYMBOLS);
        assertThat(cmds.get(0).getArg("param2")).isEqualTo(CmdParser.SYMBOLS);
        assertThat(cmds.get(0).getArg("param3")).isEqualTo(CmdParser.SYMBOLS);
    }

    @Test
    public void testCatchesShortReadErrors() {
        assertThrows(BasicError.class,() -> CmdParser.parse("start param1=\"shortread"),
            "an error should be thrown if end of input is reached in the middle of a double-quoted value.");
        assertThrows(BasicError.class,() -> CmdParser.parse("start param1='shortread"),
            "an error should be thrown if end of input is reached in the middle of a single-quoted value.");
        assertThrows(BasicError.class,() -> CmdParser.parse("param1=value1"),
            "an error should be thrown if a named parameter is specified without a prior command.");
    }

    @Test
    public void testThatSymbolsAreQuotedInStringForm() {
        List<Cmd> cmds = CmdParser.parse("start param1=value1 param2='~should be quoted'");
        assertThat(cmds.size()).isEqualTo(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(Cmd.CmdType.start);
        assertThat(cmds.get(0).getArg("param1")).isEqualTo("value1");
        assertThat(cmds.get(0).getArg("param2")).isEqualTo("~should be quoted");
        assertThat(cmds.get(0).toString()).isEqualTo("start param1=value1 param2='~should be quoted'");
    }

}

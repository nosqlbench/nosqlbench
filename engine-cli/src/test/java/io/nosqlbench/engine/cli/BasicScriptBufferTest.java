/*
 * Copyright (c) 2022 nosqlbench
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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BasicScriptBufferTest {

    @Test
    public void testScriptInterpolation() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "script_to_interpolate", "parameter1=replaced"});

        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("let foo=replaced;");
        assertThat(s).contains("let bar=UNSET:parameter2");
    }

    @Test
    public void testAutoScriptCommand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "acommand" });
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("acommand script text");
    }

    @Test
    public void testScriptParamsSingle() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "testscripts/printscript.js",
            "param1=value1"
        });
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Test
    public void testScriptParamsMulti() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "testscripts/printscript.js",
            "param1=value1",
            "script",
            "testscripts/printparam.js",
            "paramname=another",
            "param2=andanother"
        });
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Test
    public void shouldThrowErrorForInvalidWaitMillisOperand() {
        assertThatExceptionOfType(NumberFormatException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "waitmillis", "noway" }));
    }
}

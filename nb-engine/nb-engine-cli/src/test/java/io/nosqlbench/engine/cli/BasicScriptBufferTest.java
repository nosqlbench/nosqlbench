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

import io.nosqlbench.engine.cmdstream.BasicScriptBuffer;
import io.nosqlbench.engine.cmdstream.Cmd;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Tag("unit")
public class BasicScriptBufferTest {

    @Test
    public void testScriptInterpolation() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "path=script_to_interpolate", "parameter1=replaced"}, NBCLIOptions.Mode.ParseAllOptions);

        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("let foo=replaced;");
        assertThat(s).contains("let bar=UNSET:parameter2");
    }

    @Test
    public void testAutoScriptCommand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "script","path=acommand" }, NBCLIOptions.Mode.ParseAllOptions);
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("acommand script text");
    }

    @Test
    public void testScriptParamsSingle() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "path=testscripts/printscript.js",
            "param1=value1",
        }, NBCLIOptions.Mode.ParseAllOptions);
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Test
    public void testScriptParamsMulti() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "path=testscripts/printscript.js",
            "param1=value1",
            "script",
            "path=testscripts/printparam.js",
            "paramname=another",
            "param2=andanother"
        }, NBCLIOptions.Mode.ParseAllOptions);
        BasicScriptBuffer b = new BasicScriptBuffer();
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Disabled("semantic parsing is deferred till later")
    @Test
    public void shouldThrowErrorForInvalidWaitMillisOperand() {
        assertThatExceptionOfType(NumberFormatException.class)
                .isThrownBy(() -> new NBCLIOptions(new String[]{ "waitmillis", "ms=noway" }, NBCLIOptions.Mode.ParseAllOptions));
    }
}

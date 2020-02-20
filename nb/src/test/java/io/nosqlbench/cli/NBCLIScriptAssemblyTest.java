/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.cli;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIScriptAssemblyTest {

    @Test
    public void testScriptParamsSingle() {
        EBCLIOptions opts = new EBCLIOptions(new String[] {
                "script",
                "testscripts/printscript.js",
                "param1=value1"
        });
        EBCLIScriptAssembly.ScriptData sd = EBCLIScriptAssembly.assembleScript(opts);
        String assembledScript = sd.getScriptTextIgnoringParams();
        assertThat(assembledScript).matches("(?s).*a single line.*");
    }

    @Test
    public void testScriptParamsMulti() {
        EBCLIOptions opts = new EBCLIOptions(new String[] {
                "script",
                "testscripts/printscript.js",
                "param1=value1",
                "script",
                "testscripts/printparam.js",
                "paramname=another",
                "param2=andanother"
        });
        EBCLIScriptAssembly.ScriptData sd = EBCLIScriptAssembly.assembleScript(opts);
        String assembledScript = sd.getScriptTextIgnoringParams();
        assertThat(assembledScript).matches("(?s).*a single line.*");
    }

}
package io.nosqlbench.core;

import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import io.nosqlbench.engine.core.script.Scenario;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/*
*   Copyright 2016 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
public class ScenarioTest {

    @Test
    public void shouldLoadScriptText() {
        ScriptEnvBuffer buffer = new ScriptEnvBuffer();
        Scenario env = new Scenario("testing", Scenario.Engine.Graalvm);
        env.addScriptText("print('loaded script environment...');\n");
        env.run();
        assertThat(env.getIOLog().get().get(0)).contains("loaded script environment...");
    }

}

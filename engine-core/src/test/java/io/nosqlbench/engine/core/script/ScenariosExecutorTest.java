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

package io.nosqlbench.engine.core.script;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ScenariosExecutorTest {

    @Test
    @Disabled
    public void testAwaitOnTime() {
        ScenariosExecutor e = new ScenariosExecutor(new TestComponent("id","test-await-on-time"),ScenariosExecutorTest.class.getSimpleName(), 1);
        NBScriptedScenario scenario = NBScriptedScenario.ofScripted("testing", Map.of(),new TestComponent("scripted-scenario","scripted-scenario"), NBScriptedScenario.Invocation.EXECUTE_SCRIPT);
        scenario.addScriptText("load('classpath:scripts/asyncs.js');\nsetTimeout(\"print('waited')\",5000);\n");
        e.execute(scenario);
        ScenariosResults scenariosResults = e.awaitAllResults();
    }



}

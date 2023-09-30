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
import io.nosqlbench.engine.core.lifecycle.scenario.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosResults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ScenariosExecutorTest {

    @Test
    @Disabled
    public void testAwaitOnTime() {
        ScenariosExecutor e = new ScenariosExecutor(ScenariosExecutorTest.class.getSimpleName(), 1);
        NBScenario s = NBScenario.forTesting("testing", "stdout:3000", new TestComponent());
        s.addScriptText("load('classpath:scripts/asyncs.js');\nsetTimeout(\"print('waited')\",5000);\n");
        e.execute(s);
        ScenariosResults scenariosResults = e.awaitAllResults();
    }



}

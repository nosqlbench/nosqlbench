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

package io.nosqlbench.nbr.examples.injavascript;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cli.NBCLIOptions;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.session.NBSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Map;

@Disabled
@Execution(ExecutionMode.SAME_THREAD)
public class ScenarioExampleTests {

    public static ExecutionResult runScenarioTest(String... params) {
        List<Content<?>> sources = NBIO.fs().pathname(params[0]).extensionSet(".yaml", ".YAML", ".yml", ".YML").list();
        if (sources.size()!=1) {
            throw new RuntimeException("Found [" + sources.size() +"] sources for '" + params[0] +"'");
        }

        NBCLIOptions parser = new NBCLIOptions(params, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> commands = parser.getCommands();
        var myroot = new TestComponent("test_"+params[0]);
        NBSession session = new NBSession(myroot,"session_"+params[0], Map.of());
        System.out.println("=".repeat(29) + " Running scenario test for example scenario: " + params[0]);
        ExecutionResult result = session.apply(commands);
        return result;
    }

    @BeforeAll
    public static void logit() {
        System.out.println("Running ASYNC version of Script Integration Tests.");
    }

    @Test
    public void testBasicNamedScenario() {
        ExecutionResult scenarioResult = runScenarioTest("basic_scenario");
//        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*",
//            Pattern.DOTALL);
//        assertThat(p.matcher(scenarioResult.getIOLog()).matches()).isTrue();
    }

}

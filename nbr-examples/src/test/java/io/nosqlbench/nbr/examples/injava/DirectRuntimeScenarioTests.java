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

package io.nosqlbench.nbr.examples.injava;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenarioResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectRuntimeScenarioTests {

    private final TestComponent testC = new TestComponent("testroot", "testroot");
    @Disabled("enable before merge")
    @Test
    public void testDirect() {
        TestComponent testC = new TestComponent("testroot", "testroot");
        SC_activity_error sc1 = new SC_activity_error(TestComponent.EMPTY_COMPONENT, "test");
        ScenariosExecutor executor = new ScenariosExecutor(TestComponent.EMPTY_COMPONENT, "test", 1);
        executor.execute(sc1, Map.of());
        ScenariosResults results = executor.awaitAllResults();
        System.out.println(results);
    }
    @Test
    public void test_SC_linkedinput() {
        NBScenario scenario = new SC_linkedinput(testC,"test_SC_linkedinput");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*", Pattern.DOTALL);
        assertThat(p.matcher(result.getIOLog()).matches()).isTrue();

    }
    @Test
    public void testSC_activity_init_error() {
        SC_activity_init_error scenario = new SC_activity_init_error(testC, "SC_activity_init_error");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("Unknown config parameter 'unknown_config'");
        assertThat(result.getException()).isNotNull();
    }


    @Test
    public void test_SC_activity_error() {
        NBScenario scenario = new SC_activity_error(testC,"test_SC_activity_error");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("For input string: \"unparsable\"");

    }
    @Test
    public void test_SC_await_finished() {
        NBScenario scenario = new SC_await_finished(testC,"test_SC_await_finished");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        assertThat(result.getIOLog()).contains("awaited activity");
    }
    @Test
    public void test_SC_basicdiag() {
        NBScenario scenario = new SC_basicdiag(testC,"test_SC_basicdiag");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        assertThat(result.getIOLog().indexOf("starting activity basic_diag")).isGreaterThanOrEqualTo(0);
        assertThat(result.getIOLog().indexOf("stopping activity basic_diag")).isGreaterThanOrEqualTo(1);
        assertThat(result.getIOLog().indexOf("stopped activity basic_diag")).isGreaterThanOrEqualTo(2);
    }
    @Test
    public void test_SC_blockingrun() {
        NBScenario scenario = new SC_blockingrun(testC,"test_SC_blockingrun");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        assertThat(result.getIOLog()).matches(Pattern.compile(".*running.*finished.*running.*finished.*",Pattern.DOTALL));
    }



    @Test
    public void test_SC_cocycledelay_bursty() {
        NBScenario scenario = new SC_cocycledelay_bursty(testC,"test_SC_cocycledelay_bursty");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        result.report();
    }


    @Disabled("enable before merge")
    @Test
    public void test_SC_cocycledelay_strict() {
        NBScenario scenario = new SC_cocycledelay_strict(testC,"test_SC_cocycledelay_strict");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }

    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate() {
        NBScenario scenario = new SC_cycle_rate(testC,"test_SC_cycle_rate");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate_change() {
        NBScenario scenario = new SC_cycle_rate_change(testC,"test_SC_cycle_rate_change");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvmetrics() {
        NBScenario scenario = new SC_extension_csvmetrics(testC,"test_SC_extension_csvmetrics");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvoutput() {
        NBScenario scenario = new SC_extension_csvoutput(testC,"test_SC_extension_csvoutput");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_histostatslogger() {
        NBScenario scenario = new SC_extension_histostatslogger(testC,"test_SC_extension_histostatslogger");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_shutdown_hook() {
        NBScenario scenario = new SC_extension_shutdown_hook(testC,"test_SC_extension_shutdown_hook");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Test
    public void test_SC_extension_example() {
        NBScenario scenario = new SC_extension_example(testC,"test_SC_extension_example");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
//        result.exitWithCode();
        assertThat(result.getIOLog()).contains("3+5=8");
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_histologger() {
        NBScenario scenario = new SC_histologger(testC,"test_SC_histologger");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_optimo() {
        NBScenario scenario = new SC_optimo_test(testC,"test_SC_optimo");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
        System.out.println(result);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_params_variable() {
        NBScenario scenario = new SC_params_variable(testC,"test_SC_params_variable");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_readmetrics() {
        NBScenario scenario = new SC_readmetrics(testC,"test_SC_readmetrics");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_speedcheck() {
        NBScenario scenario = new SC_speedcheck(testC,"test_SC_speedcheck");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_start_stop_diag() {
        NBScenario scenario = new SC_start_stop_diag(testC,"test_SC_start_stop_diag");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Test
    public void test_SC_threadchange() {
        NBScenario scenario = new SC_threadchange(testC,"test_SC_threadchange");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_threadspeeds() {
        NBScenario scenario = new SC_threadspeeds(testC,"test_SC_threadspeeds");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario));
    }

    @Test
    public void test_SC_undef_param() {
        NBScenario scenario = new SC_undef_param(testC, "test_SC_undef_param");
        ScenarioResult result = scenario.apply(NBSceneBuffer.traced(scenario).params(Map.of("one", "two", "three", "four")));
        String out = result.getIOLog();
        assertThat(out).matches(Pattern.compile(".*after overriding .*:null.*",Pattern.DOTALL));
    }
}

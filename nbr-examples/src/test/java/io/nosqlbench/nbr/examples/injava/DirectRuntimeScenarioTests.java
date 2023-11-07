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
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenarioPhase;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenarioPhaseResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectRuntimeScenarioTests {

    private final TestComponent testC = new TestComponent("testroot", "testroot");
    @Test
    public void test_SC_linkedinput() {
        NBScenarioPhase scenario = new SC_linkedinput(testC,"test_SC_linkedinput");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*", Pattern.DOTALL);
        assertThat(p.matcher(result.getIOLog()).matches()).isTrue();

    }
    @Test
    public void testSC_activity_init_error() {
        SC_activity_init_error scenario = new SC_activity_init_error(testC, "SC_activity_init_error");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("Unknown config parameter 'unknown_config'");
        assertThat(result.getException()).isNotNull();
    }


    @Test
    public void test_SC_activity_error() {
        NBScenarioPhase scenario = new SC_activity_error(testC,"test_SC_activity_error");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("For input string: \"unparsable\"");

    }
    @Test
    public void test_SC_await_finished() {
        NBScenarioPhase scenario = new SC_await_finished(testC,"test_SC_await_finished");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        assertThat(result.getIOLog()).contains("awaited activity");
    }
    @Test
    public void test_SC_basicdiag() {
        NBScenarioPhase scenario = new SC_basicdiag(testC,"test_SC_basicdiag");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        assertThat(result.getIOLog().indexOf("starting activity basic_diag")).isGreaterThanOrEqualTo(0);
        assertThat(result.getIOLog().indexOf("stopping activity basic_diag")).isGreaterThanOrEqualTo(1);
        assertThat(result.getIOLog().indexOf("stopped activity basic_diag")).isGreaterThanOrEqualTo(2);
    }
    @Test
    public void test_SC_blockingrun() {
        NBScenarioPhase scenario = new SC_blockingrun(testC,"test_SC_blockingrun");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        assertThat(result.getIOLog()).matches(Pattern.compile(".*running.*finished.*running.*finished.*",Pattern.DOTALL));
    }



    @Test
    public void test_SC_cocycledelay_bursty() {
        NBScenarioPhase scenario = new SC_cocycledelay_bursty(testC,"test_SC_cocycledelay_bursty");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        result.report();
    }


    @Disabled("enable before merge")
    @Test
    public void test_SC_cocycledelay_strict() {
        NBScenarioPhase scenario = new SC_cocycledelay_strict(testC,"test_SC_cocycledelay_strict");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }

    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate() {
        NBScenarioPhase scenario = new SC_cycle_rate(testC,"test_SC_cycle_rate");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate_change() {
        NBScenarioPhase scenario = new SC_cycle_rate_change(testC,"test_SC_cycle_rate_change");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvmetrics() {
        NBScenarioPhase scenario = new SC_extension_csvmetrics(testC,"test_SC_extension_csvmetrics");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvoutput() {
        NBScenarioPhase scenario = new SC_extension_csvoutput(testC,"test_SC_extension_csvoutput");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_histostatslogger() {
        NBScenarioPhase scenario = new SC_extension_histostatslogger(testC,"test_SC_extension_histostatslogger");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_shutdown_hook() {
        NBScenarioPhase scenario = new SC_extension_shutdown_hook(testC,"test_SC_extension_shutdown_hook");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Test
    public void test_SC_extension_example() {
        NBScenarioPhase scenario = new SC_extension_example(testC,"test_SC_extension_example");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
//        samples.exitWithCode();
        assertThat(result.getIOLog()).contains("3+5=8");
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_histologger() {
        NBScenarioPhase scenario = new SC_histologger(testC,"test_SC_histologger");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_optimo() {
        NBScenarioPhase scenario = new SC_optimo_test(testC,"test_SC_optimo");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        System.out.println(result);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_params_variable() {
        NBScenarioPhase scenario = new SC_params_variable(testC,"test_SC_params_variable");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_readmetrics() {
        NBScenarioPhase scenario = new SC_readmetrics(testC,"test_SC_readmetrics");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_speedcheck() {
        NBScenarioPhase scenario = new SC_speedcheck(testC,"test_SC_speedcheck");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_start_stop_diag() {
        NBScenarioPhase scenario = new SC_start_stop_diag(testC,"test_SC_start_stop_diag");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Test
    public void test_SC_threadchange() {
        NBScenarioPhase scenario = new SC_threadchange(testC,"test_SC_threadchange");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_threadspeeds() {
        NBScenarioPhase scenario = new SC_threadspeeds(testC,"test_SC_threadspeeds");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
    }

    @Test
    public void test_SC_undef_param() {
        //.params(Map.of("one", "two", "three", "four")
        NBScenarioPhase scenario = new SC_undef_param(testC, "test_SC_undef_param");
        ScenarioPhaseResult result = scenario.apply(NBBufferedScenarioContext.traced(scenario));
        String out = result.getIOLog();
        assertThat(out).matches(Pattern.compile(".*after overriding .*:null.*",Pattern.DOTALL));
    }
}

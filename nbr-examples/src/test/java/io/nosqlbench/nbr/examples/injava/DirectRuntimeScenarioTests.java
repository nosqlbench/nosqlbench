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
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectRuntimeScenarioTests {

    private final TestComponent testC = new TestComponent("testroot", "testroot");
    @Test
    public void test_SC_linkedinput() {
        NBCommand scenario = new NB_linkedinput(testC,"test_SC_linkedinput");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*", Pattern.DOTALL);
        assertThat(p.matcher(result.getIOLog()).matches()).isTrue();

    }
    @Test
    public void testSC_activity_init_error() {
        NB_activity_init_error scenario = new NB_activity_init_error(testC, "SC_activity_init_error");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("Unknown config parameter 'unknown_config'");
        assertThat(result.getException()).isNotNull();
    }


    @Test
    public void test_SC_activity_error() {
        NBCommand scenario = new NB_activity_error(testC,"test_SC_activity_error");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("For input string: \"unparsable\"");

    }
    @Test
    public void test_SC_await_finished() {
        NBCommand scenario = new NB_await_finished(testC,"test_SC_await_finished");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        assertThat(result.getIOLog()).contains("awaited activity");
    }
    @Test
    public void test_SC_basicdiag() {
        NBCommand scenario = new NB_basicdiag(testC,"test_SC_basicdiag");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        assertThat(result.getIOLog().indexOf("starting activity basic_diag")).isGreaterThanOrEqualTo(0);
        assertThat(result.getIOLog().indexOf("stopping activity basic_diag")).isGreaterThanOrEqualTo(1);
        assertThat(result.getIOLog().indexOf("stopped activity basic_diag")).isGreaterThanOrEqualTo(2);
    }
    @Test
    public void test_SC_blockingrun() {
        NBCommand scenario = new NB_blockingrun(testC,"test_SC_blockingrun");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        assertThat(result.getIOLog()).matches(Pattern.compile(".*running.*finished.*running.*finished.*",Pattern.DOTALL));
    }



    @Test
    public void test_SC_cocycledelay_bursty() {
        NBCommand scenario = new NB_cocycledelay_bursty(testC,"test_SC_cocycledelay_bursty");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        result.report();
    }


    @Disabled("enable before merge")
    @Test
    public void test_SC_cocycledelay_strict() {
        NBCommand scenario = new NB_cocycledelay_strict(testC,"test_SC_cocycledelay_strict");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }

    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate() {
        NBCommand scenario = new NB_cycle_rate(testC,"test_SC_cycle_rate");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate_change() {
        NBCommand scenario = new NB_cycle_rate_change(testC,"test_SC_cycle_rate_change");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvmetrics() {
        NBCommand scenario = new NB_extension_csvmetrics(testC,"test_SC_extension_csvmetrics");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvoutput() {
        NBCommand scenario = new NB_extension_csvoutput(testC,"test_SC_extension_csvoutput");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_histostatslogger() {
        NBCommand scenario = new NB_extension_histostatslogger(testC,"test_SC_extension_histostatslogger");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_shutdown_hook() {
        NBCommand scenario = new NB_extension_shutdown_hook(testC,"test_SC_extension_shutdown_hook");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Test
    public void test_SC_extension_example() {
        NBCommand scenario = new NB_extension_example(testC,"test_SC_extension_example");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
//        samples.exitWithCode();
        assertThat(result.getIOLog()).contains("3+5=8");
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_histologger() {
        NBCommand scenario = new NB_histologger(testC,"test_SC_histologger");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_optimo() {
        NBCommand scenario = new NB_optimo_test(testC,"test_SC_optimo");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        System.out.println(result);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_params_variable() {
        NBCommand scenario = new NB_params_variable(testC,"test_SC_params_variable");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_readmetrics() {
        NBCommand scenario = new NB_readmetrics(testC,"test_SC_readmetrics");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_speedcheck() {
        NBCommand scenario = new NB_speedcheck(testC,"test_SC_speedcheck");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_start_stop_diag() {
        NBCommand scenario = new NB_start_stop_diag(testC,"test_SC_start_stop_diag");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Test
    public void test_SC_threadchange() {
        NBCommand scenario = new NB_threadchange(testC,"test_SC_threadchange");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_threadspeeds() {
        NBCommand scenario = new NB_threadspeeds(testC,"test_SC_threadspeeds");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
    }

    @Test
    public void test_SC_undef_param() {
        //.params(Map.of("one", "two", "three", "four")
        NBCommand scenario = new NB_undef_param(testC, "test_SC_undef_param");
        NBCommandResult result = scenario.apply(NBBufferedCommandContext.traced(scenario), NBCommandParams.of(Map.of()));
        String out = result.getIOLog();
        assertThat(out).matches(Pattern.compile(".*after overriding .*:null.*",Pattern.DOTALL));
    }
}

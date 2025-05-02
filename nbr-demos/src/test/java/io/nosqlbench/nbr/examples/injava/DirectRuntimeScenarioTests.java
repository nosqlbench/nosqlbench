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

package io.nosqlbench.nbr.examples.injava;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.session.NBCommandInvoker;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectRuntimeScenarioTests {


    public void test_NB_session_metrics() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_NB_session_metrics").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_session_metrics(testC,"test_NB_session_metrics");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getIOLog()).contains("started activity");
        assertThat(result.getIOLog()).contains("stopped activity");
    }
    private final TestComponent testC = new TestComponent("testroot", "testroot");
    @Test
    public void test_SC_linkedinput() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_linkedinput").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_linkedinput(testC,"test_SC_linkedinput");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*", Pattern.DOTALL);
        assertThat(p.matcher(result.getIOLog()).matches()).isTrue();

    }
    @Test
    public void testSC_activity_init_error() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("testSC_activity_init_error").build(TestComponent.EMPTY_COMPONENT);
        NB_activity_init_error command = new NB_activity_init_error(testC, "SC_activity_init_error");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("Unknown config parameter 'unknown_config'");
        assertThat(result.getException()).isNotNull();
    }


    @Test
    public void test_SC_activity_error() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_activity_error").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_activity_error(testC,"test_SC_activity_error");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getMessage()).contains("For input string: \"unparsable\"");

    }
    @Test
    public void test_SC_await_finished() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_await_finished").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_await_finished(testC,"test_SC_await_finished");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getIOLog()).contains("awaited activity");
    }
    @Test
    public void test_SC_basicdiag() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_basicdiag").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_basicdiag(testC,"test_SC_basicdiag");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getIOLog().indexOf("starting activity basic_diag")).isGreaterThanOrEqualTo(0);
        assertThat(result.getIOLog().indexOf("stopping activity basic_diag")).isGreaterThanOrEqualTo(1);
        assertThat(result.getIOLog().indexOf("stopped activity basic_diag")).isGreaterThanOrEqualTo(2);
    }
    @Test
    public void test_SC_blockingrun() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_blockingrun").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_blockingrun(testC,"test_SC_blockingrun");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        assertThat(result.getIOLog()).matches(Pattern.compile(".*running.*finished.*running.*finished.*",Pattern.DOTALL));
    }



    @Test
    public void test_SC_cocycledelay_bursty() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_cocycledelay_bursty").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_cocycledelay_bursty(testC,"test_SC_cocycledelay_bursty");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        result.report();
    }


    @Disabled("enable before merge")
    @Test
    public void test_SC_cocycledelay_strict() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("testest_SC_cocycledelay_strictting").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_cocycledelay_strict(testC,"test_SC_cocycledelay_strict");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }

    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_cycle_rate").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_cycle_rate(testC,"test_SC_cycle_rate");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_cycle_rate_change() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_cycle_rate_change").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_cycle_rate_change(testC,"test_SC_cycle_rate_change");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvmetrics() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_extension_csvmetrics").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_extension_csvmetrics(testC,"test_SC_extension_csvmetrics");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_csvoutput() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_extension_csvoutput").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_extension_csvoutput(testC,"test_SC_extension_csvoutput");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_histostatslogger() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_extension_histostatslogger").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_extension_histostatslogger(testC,"test_SC_extension_histostatslogger");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_extension_shutdown_hook() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_extension_shutdown_hook").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_extension_shutdown_hook(testC,"test_SC_extension_shutdown_hook");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Test
    public void test_SC_extension_example() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_extension_example").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_extension_example(testC,"test_SC_extension_example");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        //        samples.exitWithCode();
        assertThat(result.getIOLog()).contains("3+5=8");
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_histologger() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("tetest_SC_histologgersting").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_histologger(testC,"test_SC_histologger");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_optimo() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_optimo").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_optimo_test(testC,"test_SC_optimo");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        System.out.println(result);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_params_variable() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_params_variable").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_params_variable(testC,"test_SC_params_variable");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_readmetrics() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_readmetrics").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_readmetrics(testC,"test_SC_readmetrics");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_speedcheck() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_speedcheck").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_speedcheck(testC,"test_SC_speedcheck");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_start_stop_diag() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_start_stop_diag").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_start_stop_diag(testC,"test_SC_start_stop_diag");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Test
    public void test_SC_threadchange() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_threadchange").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_threadchange(testC,"test_SC_threadchange");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }
    @Disabled("enable before merge")
    @Test
    public void test_SC_threadspeeds() {
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_threadspeeds").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_threadspeeds(testC,"test_SC_threadspeeds");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
    }

    @Test
    public void test_SC_undef_param() {
        //.params(Map.of("one", "two", "three", "four")
        NBBufferedContainer testC = NBBufferedContainer.builder().name("test_SC_undef_param").build(TestComponent.EMPTY_COMPONENT);
        NBBaseCommand command = new NB_undef_param(testC, "test_SC_undef_param");
        NBCommandResult result = NBCommandInvoker.invoke(testC,command);
        String out = result.getIOLog();
        assertThat(out).matches(Pattern.compile(".*after overriding .*:null.*",Pattern.DOTALL));
    }
}

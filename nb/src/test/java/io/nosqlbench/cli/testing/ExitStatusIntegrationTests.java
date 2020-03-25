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

package io.nosqlbench.cli.testing;

import org.testng.annotations.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class ExitStatusIntegrationTests {

    private final static String JARNAME = "target/nb.jar";
    @Test
    public void testExitStatusOnBadParam() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_badparam", 15,
                "java", "-jar", JARNAME, "--logs-dir", "logs/test", "badparam"
        );
        String stderr = result.getStderrData().stream().collect(Collectors.joining("\n"));
        assertThat(stderr).contains("unrecognized option:badparam");
        assertThat(result.exitStatus).isEqualTo(1);
    }

    @Test
    public void testExitStatusOnActivityInitException() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_initexception", 15,
                "java", "-jar", JARNAME, "--logs-dir", "logs/test", "run", "driver=diag", "initdelay=notanumber"
        );
        String stderr = result.getStdoutData().stream().collect(Collectors.joining("\n"));
        assertThat(stderr).contains("Error initializing activity 'ALIAS_UNSET': For input string: \"notanumber\"");
        assertThat(result.exitStatus).isEqualTo(2);
    }

// Temporarily disabled for triage
// TODO: figure out if github actions is an issue for this test.
// It passes locally, but fails spuriously in github actions runner
//    @Test
//    public void testExitStatusOnActivityThreadException() {
//        ProcessInvoker invoker = new ProcessInvoker();
//        invoker.setLogDir("logs/test");
//        ProcessResult result = invoker.run("exitstatus_threadexception", 30,
//                "java", "-jar", JARNAME, "--logs-dir", "logs/test", "run", "driver=diag", "throwoncycle=10", "cycles=1000", "cyclerate=10", "-vvv"
//        );
//        String stdout = result.getStdoutData().stream().collect(Collectors.joining("\n"));
//        assertThat(stdout).contains("Diag was asked to throw an error on cycle 10");
//        assertThat(result.exitStatus).isEqualTo(2);
//    }

    @Test
    public void testExitStatusOnActivityAsyncStopException() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_asyncstoprequest", 30,
                "java", "-jar", JARNAME, "--logs-dir", "logs/test", "run", "driver=diag", "async=1", "cyclerate=5", "erroroncycle=10", "cycles=2000", "-vvv"
        );
        String stdout = result.getStdoutData().stream().collect(Collectors.joining("\n"));
        assertThat(stdout).contains("Diag was requested to stop on cycle 10");
        assertThat(result.exitStatus).isEqualTo(2);
    }



}

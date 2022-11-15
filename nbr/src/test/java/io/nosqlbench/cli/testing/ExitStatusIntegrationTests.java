/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.cli.testing;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExitStatusIntegrationTests {

    private final String java = Optional.ofNullable(System.getenv(
            "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");

    private final static String JARNAME = "target/nbr.jar";

    @Test
    void testExitStatusOnBadParam() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_badparam", 15,
                java, "-jar", JARNAME, "--logs-dir", "logs/test/badparam/",
                "badparam"
        );
        assertThat(result.exception).isNull();
        String stderr = String.join("\n", result.getStderrData());
        assertThat(stderr).contains("Scenario stopped due to error");
        assertThat(result.exitStatus).isEqualTo(2);
    }

    @Test
    void testExitStatusOnActivityInitException() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_initexception", 15,
                java, "-jar", JARNAME, "--logs-dir", "logs/test/initerror", "run",
                "driver=diag", "op=initdelay:initdelay=notanumber"
        );
        assertThat(result.exception).isNull();
        String stderr = String.join("\n", result.getStdoutData());
        assertThat(stderr).contains("For input string: \"notanumber\"");
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
    void testExitStatusOnActivityOpException() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("exitstatus_asyncstoprequest", 30,
                java, "-jar", JARNAME, "--logs-dir", "logs/test/asyncstop", "run",
                "driver=diag", "cyclerate=1", "op=erroroncycle:erroroncycle=10", "cycles=2000", "-vvv"
        );
        assertThat(result.exception).isNull();
        String stdout = String.join("\n", result.getStdoutData());
        assertThat(stdout).contains("Diag was requested to stop on cycle 10");
        assertThat(result.exitStatus).isEqualTo(2);
    }


}

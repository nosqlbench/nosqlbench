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

package io.nosqlbench.engine.core.script;

import io.nosqlbench.cli.testing.ProcessInvoker;
import io.nosqlbench.cli.testing.ProcessResult;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCliIntegrationTests {

    private final static String JARNAME = "target/nbr.jar";
    private final Logger logger = LogManager.getLogger(NBCliIntegrationTests.class);
    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v+"/bin/java").orElse("java");


    @Test
    public void listWorkloadsTest() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run(
                "workload-test", 15, java, "-jar",
                JARNAME, "--logs-dir", "logs/test", "--list-workloads", "--show-stacktraces"
        );
        System.out.println(result.getStdoutData());
        System.out.println(result.getStderrData());
        assertThat(result.exitStatus).isEqualTo(0);
    }
}

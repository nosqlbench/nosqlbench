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

package io.nosqlbench.engine.api.scenarios;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class NBCLIScenarioPreprocessorTest {

    @Test
    public void rewriteScenarioCommands_shouldNotTriggerDryrunDuringScenarioParsing() throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(
            javaBin,
            "-cp",
            classpath,
            "io.nosqlbench.engine.api.scenarios.NBCLIScenarioPreprocessorDryrunProbe"
        );
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectErrorStream(true);

        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = p.waitFor();

        assertThat(exit).isEqualTo(0);
        assertThat(output).contains("PROBE_OK");
        assertThat(output).contains("cmd_line_param=firstone");
        assertThat(output).contains("dryrun=exprs");
        assertThat(output).contains("run");
    }
}

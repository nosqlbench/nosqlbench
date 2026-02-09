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

import java.util.LinkedList;
import java.util.List;

/**
 * Probe used by NBCLIScenarioPreprocessorTest to ensure dryrun does not
 * trigger System.exit during scenario parsing.
 */
public class NBCLIScenarioPreprocessorDryrunProbe {
    public static void main(String[] args) {
        LinkedList<String> argv = new LinkedList<>();
        argv.add("cmdline_template");
        argv.add("the_scenario");
        argv.add("dryrun=exprs");

        NBCLIScenarioPreprocessor.rewriteScenarioCommands(argv, List.of("src/test/resources"));

        System.out.println("PROBE_OK");
        System.out.println("ARGS=" + String.join(" ", argv));
    }
}

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

package io.nosqlbench.scenarios.diag;

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommand;
import io.nosqlbench.nb.annotations.Service;

import java.io.PrintWriter;
import java.io.Reader;

@Service(value = NBCommand.class, selector = "diag")
public class NB_diag extends NBCommand {
    public NB_diag(NBComponent parentComponent, String scenarioName, String targetScenario) {
        super(parentComponent, scenarioName, targetScenario);
    }

    @Override
    public void invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ScenarioActivitiesController controller) {
        stdout.println("diagnostic scenario writing params to stdout:");
        params.forEach((k, v) -> {
            stdout.println(k + ":" + v);
        });
        stderr.println("diagnostic scenario writing to stderr");

    }
}

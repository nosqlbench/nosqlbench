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

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioPhaseParams;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenarioPhase;

import java.util.Map;

public class SC_basicdiag extends SCBaseScenarioPhase {
    public SC_basicdiag(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * basic_diag = paramValues.withOverrides({
     *     "alias" : "basic_diag",
     *     "driver" : "diag"
     * });
     *
     *
     * print('starting activity basic_diag');
     * scenario.start(basic_diag);
     * }</pre>
     */
    @Override
    public void invoke(ScenarioPhaseParams params) {
        var basic_diag = params.withOverrides(
            Map.of("alias","basic_diag","driver","diag")
        );
        stdout.println("starting activity basic_diag");
        controller.start(basic_diag);
        stdout.println("stopping activity basic_diag");
        controller.stop(basic_diag);
        stdout.println("stopped activity basic_diag");
    }
}

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

public class SC_params_variable extends SCBaseScenarioPhase {
    public SC_params_variable(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * print('paramValues["one"]=\'' + paramValues["one"] + "'");
     * print('paramValues["three"]=\'' + paramValues["three"] + "'");
     *
     * var overrides = {
     *   'three': "five"
     * };
     *
     * var overridden = paramValues.withOverrides(overrides);
     *
     * print('overridden["three"] [overridden-three-five]=\'' + overridden["three"] + "'");
     *
     * var defaults = {
     *     'four': "niner"
     * };
     *
     * var defaulted = paramValues.withDefaults(defaults);
     *
     * print('defaulted.get["four"] [defaulted-four-niner]=\'' + defaulted["four"] + "'");
     * }</pre>
     */
    @Override
    public void invoke(ScenarioPhaseParams params) {

    }
}

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

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

import java.io.PrintWriter;
import java.io.Reader;


public class NB_params_variable extends NBBaseCommand {
    public NB_params_variable(NBBufferedContainer parentComponent, String scenarioName) {
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
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        return null;
    }
}

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
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;

import java.io.PrintWriter;
import java.io.Reader;


public class NB_cycle_rate extends NBBaseCommand {
    public NB_cycle_rate(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef = {
     *     "alias" : "cycle_rate",
     *     "driver" : "diag",
     *     "cycles" : "2500",
     *     "threads" : "10",
     *     "cyclerate" : "500",
     *     "op" : "noop"
     * };
     *
     * scenario.run(activitydef);
     *
     * print("current cycle = " + metrics.cycle_rate.cycles_servicetime.count);
     * print("mean cycle rate = " + metrics.cycle_rate.cycles_servicetime.meanRate);
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        return null;
    }
}

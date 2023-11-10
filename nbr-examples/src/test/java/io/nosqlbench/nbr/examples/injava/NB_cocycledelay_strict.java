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
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommand;

import java.io.PrintWriter;
import java.io.Reader;


public class NB_cocycledelay_strict extends NBCommand {
    public NB_cocycledelay_strict(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }


    /** <pre>{@code
     * co_cycle_delay = {
     *     "alias" : "co_cycle_delay",
     *     "driver" : "diag",
     *     "cycles" : "0..10000",
     *     "threads" : "1",
     *     "cyclerate" : "1000,1.0",
     *     "op" : "diagrate:diagrate=800"
     * };
     *
     * print('starting activity co_cycle_delay');
     * scenario.start(co_cycle_delay);
     * scenario.waitMillis(4000);
     * print('step1 cycles_waittime=' + metrics.co_cycle_delay.cycles_waittime.value);
     * activities.co_cycle_delay.diagrate="10000";
     * for(i=0;i<5;i++) {
     *     if (! scenario.isRunningActivity('co_cycle_delay')) {
     *         print("scenario exited prematurely, aborting.");
     *         break;
     *     }
     *     print("iteration " + i + " waittime now " + metrics.co_cycle_delay.cycles_waittime.value);
     *     scenario.waitMillis(1000);
     * }
     *
     *
     * //scenario.awaitActivity("co_cycle_delay");
     * print('step2 cycles_waittime=' + metrics.co_cycle_delay.cycles_waittime.value);
     * print("awaited activity");
     * }</pre>
     */
    @Override
    public void invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ScenarioActivitiesController controller) {

    }

}

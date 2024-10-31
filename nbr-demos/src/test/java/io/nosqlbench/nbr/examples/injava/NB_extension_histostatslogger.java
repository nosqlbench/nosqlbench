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
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;

import java.io.PrintWriter;
import java.io.Reader;


public class NB_extension_histostatslogger extends NBBaseCommand {
    public NB_extension_histostatslogger(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef = {
     *     "alias" : "testhistostatslogger",
     *     "driver" : "diag",
     *     "cycles" : "50000",
     *     "threads" : "5",
     *     "rate" : "100.0",
     *     "op" : "noop"
     * };
     *
     * histostatslogger.logHistoStats("testing extention histostatslogger", ".*", "logs/histostats.csv", "0.5s");
     * print("started logging to logs/histostats.csv for all metrics at 1/2" +
     *     " second intervals.");
     * scenario.start(activitydef);
     * scenario.waitMillis(4000);
     * scenario.stop(activitydef);
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        return null;
    }
}

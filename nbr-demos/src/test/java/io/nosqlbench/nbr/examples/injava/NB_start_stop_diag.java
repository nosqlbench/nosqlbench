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

import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import org.jetbrains.annotations.NotNull;


import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_start_stop_diag extends NBBaseCommand {
    public NB_start_stop_diag(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     *
     * activitydef = {
     *     "alias" : "teststartstopdiag",
     *     "driver" : "diag",
     *     "cycles" : "0..1000000000",
     *     "threads" : "5",
     *     "interval" : "2000",
     *     "op" : "noop",
     *     "rate" : "5"
     * };
     *
     * print('starting activity teststartstopdiag');
     * scenario.start(activitydef);
     *
     * print('waiting 500 ms');
     * scenario.waitMillis(500);
     *
     * print('waited, stopping activity teststartstopdiag');
     * scenario.stop(activitydef);
     *
     * print('stopped activity teststartstopdiag');
     *
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {

        Map<String, String> activitydef = Map.of(
            "alias", "teststartstopdiag", "driver", "diag", "cycles", "0..1000000000", "threads",
            "5", "interval", "2000", "op", "noop", "rate", "5"
        );

        stdout.println("starting activity teststartstopdiag");
        controller.start(activitydef);

        stdout.println("waiting 500 ms");
        controller.waitMillis(500);

        stdout.println("waited, stopping activity teststartstopdiag");
        controller.stop(activitydef);

        stdout.println("stopped activity teststartstopdiag");
        return null;
    }
}

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

import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

import io.nosqlbench.nb.annotations.Service;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

@Service(value= NBBaseCommand.class,selector="activity_init_error")
public class NB_activity_init_error extends NBBaseCommand {
    public NB_activity_init_error(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef1 = {
     *     "alias" : "activity_init_error",
     *     "driver" : "diag",
     *     "cycles" : "invalid",
     *     "threads" : "1",
     *     "targetrate" : "500",
     *     "unknown_config" : "unparsable",
     *     "op" : "noop"
     * };
     *
     * print('starting activity activity_init_error');
     * scenario.start(activitydef1);
     * scenario.waitMillis(2000);
     * scenario.awaitActivity("activity_init_error");
     * print("awaited activity");}</pre>
     *
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        var activitydef1 = Map.of(
            "alias","activity_init_error",
            "driver","diag",
            "cycles","invalid",
            "threads","1",
            "targetrate","500",
            "unknown_config","unparsable",
            "op","noop"
        );

        stdout.println("starting activity activity_init_error");
        controller.start(activitydef1);
        controller.waitMillis(2000);
        controller.awaitActivity("activity_init_error",Long.MAX_VALUE);
        stdout.println("awaited activity");
        return null;
    }
}

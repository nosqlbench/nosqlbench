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
import org.jetbrains.annotations.NotNull;


import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_activity_error extends NBBaseCommand {
    public NB_activity_error(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /**
     * Equivalent to javascript form:
     * <pre>{@code
     * activitydef1 = {
     *  "alias": "activity_error",
     *  "driver": "diag",
     *  "cycles": "0..1500000",
     *  "threads": "1",
     *  "targetrate": "10",
     *  "op": "log: modulo=1"
     * };
     *
     * print('starting activity activity_error');
     * scenario.start(activitydef1);
     * scenario.waitMillis(2000);
     * activities.activity_error.threads = "unparsable";
     * scenario.awaitActivity("activity_error");
     * print("awaited activity");
     * </pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Map<String, String> activitydef1 = Map.of(
            "alias", "activity_error", "driver", "diag", "cycles", "0..1500000", "threads", "1",
            "targetrate", "10", "op", "log: modulo=1"
        );
        stdout.write("starting activity activity_error");
        controller.start(activitydef1);
        controller.waitMillis(500);
        controller.getActivityDef("activity_error").getParams().set("threads","unparsable"); // forced error
        controller.awaitActivity("activity_error", Long.MAX_VALUE);
        return null;
    }
}

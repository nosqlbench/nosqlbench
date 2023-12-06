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

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;


import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_blockingrun extends NBBaseCommand {
    public NB_blockingrun(NBBufferedCommandContext parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef1 = {
     *     "alias" : "blockingactivity1",
     *     "driver" : "diag",
     *     "cycles" : "0..100000",
     *     "threads" : "1",
     *     "interval" : "2000",
     *     "op":"noop"
     * };
     * activitydef2 = {
     *     "alias" : "blockingactivity2",
     *     "driver" : "diag",
     *     "cycles" : "0..100000",
     *     "threads" : "1",
     *     "interval" : "2000",
     *     "op":"noop"
     * };
     *
     *
     * print('running blockingactivity1');
     * scenario.run(10000,activitydef1);
     * print('blockingactivity1 finished');
     * print('running blockingactivity2');
     * scenario.run(10000,activitydef2);
     * print('blockingactivity2 finished');
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        var activitydef1 = Map.of(
            "alias","blockactivity1","driver","diag",
            "cycles","0..10000","threads","1",
            "interval","2000","op","noop"
        );

        var activitydef2 = Map.of(
            "alias", "blockingactivity2","driver","diag",
            "cycles","0..10000","threads","1",
            "interval","2000", "op","noop"
        );

        stdout.println("running blockingactivity1");
        controller.run(activitydef1);
        stdout.println("blockingactivity1 finished");
        stdout.println("running blockingactivity2");
        controller.run(activitydef2);
        stdout.println("blockingactivity2 finished");
        return null;
    }
}

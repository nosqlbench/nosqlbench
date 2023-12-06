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
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_linkedinput extends NBBaseCommand {
    public NB_linkedinput(NBBufferedCommandContext parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /**
     * <pre>{@code
     * var leader = {
     *     driver: 'diag',
     *     alias: 'leader',
     *     targetrate: '10000',
     *     op: 'log:level=info'
     * };
     *
     * var follower = {
     *     driver: 'diag',
     *     alias: 'follower',
     *     // linkinput: 'leader',
     *     op: 'log:level=INFO'
     * };
     *
     * scenario.start(leader);
     * print("started leader");
     * scenario.start(follower);
     * print("started follower");
     *
     * scenario.waitMillis(500);
     *
     * scenario.stop(leader);
     * print("stopped leader");
     * scenario.stop(follower);
     * print("stopped follower");
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
    var leader = Map.of(
            "driver", "diag",
            "alias", "leader",
            "targetrate", "10000",
            "op", "log:level=info"
        );

        var follower = Map.of(
            "driver", "diag",
            "alias", "follower",
            "op", "log:level=INFO"

        );

        controller.start(leader);
        stdout.println("started leader");
        controller.start(follower);
        stdout.println("started follower");

        controller.waitMillis(500);
        controller.stop(leader);
        stdout.println("stopped leader");
        controller.stop(follower);
        stdout.println("stopped follower");
        return null;
    }

}

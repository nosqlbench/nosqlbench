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
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.nbr.examples.SCBaseScenario;

import java.util.Map;

public class SC_await_finished extends SCBaseScenario {
    public SC_await_finished(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /**
     * <pre>{@code
     * activitydef1 = {
     *     "alias" : "activity_to_await",
     *     "driver" : "diag",
     *     "cycles" : "0..1500",
     *     "threads" : "1",
     *     "targetrate" : "500",
     *     "op" : "noop"
     * };
     *
     * print('starting activity teststartstopdiag');
     * scenario.start(activitydef1);
     * scenario.awaitActivity("activity_to_await");
     * print("awaited activity");
     * }</pre>
     */
    @Override
    public void invoke() {
        var activitydef1 = Map.of(
            "alias", "activity_to_await",
            "driver", "diag",
            "cycles", "0..1500",
            "threads", "1",
            "targetrate", "500",
            "op", "noop"
        );
        stdout.println("starting activity activity_to_await");
        controller.start(activitydef1);
        controller.awaitActivity("activity_to_await",1000L);
        stdout.println("awaited activity");
    }
}

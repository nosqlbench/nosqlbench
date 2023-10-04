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

package io.nosqlbench.nbr.examples;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;

import java.util.Map;

public class SCDryRunScenarioTest extends NBScenario {
    public SCDryRunScenarioTest(NBComponent parentComponent, String scenarioName, Map<String, String> params, String progressInterval) {
        super(parentComponent, scenarioName, params, progressInterval);
    }

    /**
     * print('starting activity activity_error');
     * scenario.start(activitydef1);
     * scenario.waitMillis(2000);
     * activities.activity_error.threads = "unparsable";
     * scenario.awaitActivity("activity_error");
     * print("awaited activity");
     */
    @Override
    protected void runScenario(NBSceneFixtures shell) {

        /**
         * activitydef1 = {
         *     "alias": "activity_error",
         *     "driver": "diag",
         *     "cycles": "0..1500000",
         *     "threads": "1",
         *     "targetrate": "10",
         *     "op": "log: modulo=1"
         * };
         */
        var activitydef1 = Map.of("alias", "activity_error",
          "driver", "diag",
          "cycles", "0..1500000",
          "threads", "1",
          "targetrate", "10",
          "op", "log: modulo=1");


         // print('starting activity activity_error');
        shell.out().write("starting activity activity_error");

        // scenario.start(activitydef1);
        shell.controller().start(activitydef1);

         // scenario.waitMillis(2000);
        shell.controller().waitMillis(2000);

         // activities.activity_error.threads = "unparsable";
        ActivityDef def = shell.controller().getActivityDef("activity_error");
        def.getParams().set("threads","unparsable");

        // scenario.awaitActivity("activity_error");
        shell.controller().awaitActivity("activity_error", Long.MAX_VALUE);

        // print("awaited activity");

        shell.out().println("awaited activity");
    }
}

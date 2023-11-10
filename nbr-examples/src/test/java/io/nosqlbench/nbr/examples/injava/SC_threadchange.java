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
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioPhaseParams;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenarioPhase;

public class SC_threadchange extends SCBaseScenarioPhase {
    public SC_threadchange(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * scenario.start("driver=diag;alias=threadchange;cycles=0..60000;threads=1;interval=2000;op='noop';rate=1000");
     * activities.threadchange.threads=1;
     * print("threads now " + activities.threadchange.threads);
     * print('waiting 500 ms');
     * scenario.waitMillis(500);
     *
     * activities.threadchange.threads=5;
     * print("threads now " + activities.threadchange.threads);
     * scenario.stop('threadchange');
     * }</pre>
     */
    @Override
    public void invoke(ScenarioPhaseParams params) {

          var activity = controller.start("driver=diag;alias=threadchange;cycles=0..60000;threads=1;interval=2000;op='noop';rate=1000");
          activity.getActivityDef().setThreads(1);
          stdout.println("threads now " + activity.getActivityDef().getThreads());
          stdout.println("waiting 500 ms");
        controller.waitMillis(500);

        activity.getActivityDef().setThreads(5);
          stdout.println("threads now " + activity.getActivityDef().getThreads());
          controller.stop("threadchange");


    }
}

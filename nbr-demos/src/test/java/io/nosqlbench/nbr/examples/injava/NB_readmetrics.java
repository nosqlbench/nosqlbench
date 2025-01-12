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

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;


public class NB_readmetrics extends NBBaseCommand {
    public NB_readmetrics(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef = {
     *     "alias" : "testactivity",
     *     "driver" : "diag",
     *     "cycles" : "0..1000000000",
     *     "threads" : "25",
     *     "interval" : "2000",
     *     "op" : "noop"
     * };
     *
     * scenario.start(activitydef);
     *
     * scenario.waitMillis(500);
     * while (metrics.testactivity.cycles_servicetime.count < 1000) {
     *     print('waiting 10ms because cycles<10000 : ' + metrics.testactivity.cycles_servicetime.count);
     *     scenario.waitMillis(10);
     *
     * }
     * scenario.stop(activitydef);
     * print("count: " + metrics.testactivity.cycles_servicetime.count);
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Activity activity = controller.start(Map.of(
            "alias", "testactivity",
            "driver", "diag",
            "cycles", "0..1000000000",
            "threads", "5",
            "interval", "2000",
            "op", "noop"
        ));
        controller.waitMillis(500);
        NBMetricTimer service_timer = find().topMetric("activity=testactivity,name=cycles_servicetime", NBMetricTimer.class);

        while (service_timer.getCount()<1000) {
            stdout.println("waiting 10ms because cycles<1000");
            controller.waitMillis(10);
        }

        controller.stop("testactivity");
        stdout.println("count: " + service_timer.getCount());
        return null;
    }
}

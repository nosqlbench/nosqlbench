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
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;


import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_cocycledelay_bursty extends NBBaseCommand {
    public NB_cocycledelay_bursty(NBBufferedContainer parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /**
     * <pre>{@code
     * co_cycle_delay_bursty = {
     *     "alias": "co_cycle_delay_bursty",
     *     "driver": "diag",
     *     "cycles": "0..1000000",
     *     "threads": "10",
     *     "cyclerate": "1000,1.5",
     *     "op" : "diagrate: diagrate=500"
     * };
     *
     * print('starting activity co_cycle_delay_bursty');
     * scenario.start(co_cycle_delay_bursty);
     * for (i = 0; i < 5; i++) {
     *     scenario.waitMillis(1000);
     *     if (!scenario.isRunningActivity('co_cycle_delay_bursty')) {
     *         print("scenario exited prematurely, aborting.");
     *         break;
     *     }
     *     print("backlogging, cycles=" + metrics.co_cycle_delay_bursty.cycles_servicetime.count +
     *         " waittime=" + metrics.co_cycle_delay_bursty.cycles_waittime.value +
     *         " diagrate=" + activities.co_cycle_delay_bursty.diagrate +
     *         " cyclerate=" + activities.co_cycle_delay_bursty.cyclerate
     *     );
     * }
     * print('step1 metrics.waittime=' + metrics.co_cycle_delay_bursty.cycles_waittime.value);
     * activities.co_cycle_delay_bursty.diagrate = "10000";
     *
     * for (i = 0; i < 10; i++) {
     *     if (!scenario.isRunningActivity('co_cycle_delay_bursty')) {
     *         print("scenario exited prematurely, aborting.");
     *         break;
     *     }
     *     print("recovering, cycles=" + metrics.co_cycle_delay_bursty.cycles_servicetime.count +
     *         " waittime=" + metrics.co_cycle_delay_bursty.cycles_waittime.value +
     *         " diagrate=" + activities.co_cycle_delay_bursty.diagrate +
     *         " cyclerate=" + activities.co_cycle_delay_bursty.cyclerate
     *     );
     *
     *     scenario.waitMillis(1000);
     *     if (metrics.co_cycle_delay_bursty.cycles_waittime.value < 50000000) {
     *         print("waittime trended back down as expected, exiting on iteration " + i);
     *         break;
     *     }
     * }
     * //scenario.awaitActivity("co_cycle_delay");
     * print('step2 metrics.waittime=' + metrics.co_cycle_delay_bursty.cycles_waittime.value);
     * scenario.stop(co_cycle_delay_bursty);
     * print("stopped activity co_cycle_delay_bursty");
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        int diagrate = 500;
        var co_cycle_delay_bursty = Map.of(
            "alias", "co_cycle_delay_bursty",
            "driver", "diag",
            "cycles", "0..1000000",
            "threads", "1",
            "cyclerate", "1000,1.5",
            "op", "diagrate: diagrate=" + diagrate
//            "dryrun", "op" // silent
        );
        NBComponent container = this;
        while (container.getParent()!=null) {
            container=container.getParent();
        }

        controller.waitMillis(500);
        stdout.println("starting activity co_cycle_delay_bursty");
        Activity activity = controller.start(co_cycle_delay_bursty);
        controller.waitMillis(1000);

        NBMetricTimer service_time_counter = container.find().topMetric("activity=co_cycle_delay_bursty,name=cycles_servicetime", NBMetricTimer.class);
        NBMetricGauge wait_time_gauge = container.find().topMetric("activity=co_cycle_delay_bursty,name=cycles_waittime",NBMetricGauge.class);

        for (int i = 0; i < 5; i++) {
            controller.waitMillis(1000);
            if (!controller.isRunningActivity("co_cycle_delay_bursty")) {
                throw new RuntimeException("Scenario exited prematurely.");
            }

            stdout.println("backlogging, cycles=" + service_time_counter.getCount() +
                " waittime=" + wait_time_gauge.getValue() +
                " diagrate=" + diagrate +
                " cyclerate=" + activity.getCycleLimiter().getSpec()
            );
        }

        stdout.println("step1 waittime=" + wait_time_gauge.getValue());
        activity.onEvent(new ParamChange<>(new CycleRateSpec(10000, 1.1)));

        for (int i = 0; i < 10; i++) {
            if (!controller.isRunningActivity("co_cycle_delay_bursty")) {
                throw new RuntimeException("scenario exited prematurely.");
            }
            stdout.println("backlogging, cycles=" + service_time_counter.getCount() +
                " waittime=" + wait_time_gauge.getValue() +
                " diagrate=" + diagrate +
                " cyclerate=" + activity.getCycleLimiter().getSpec()
            );
            if (wait_time_gauge.getValue() < 50000000) {
                stdout.println("waittime trended back down as expected, exiting on iteration " + i);
                break;
            }
        }

        stdout.println("step2 metrics.waittime=" + wait_time_gauge.getValue());
        controller.stop(co_cycle_delay_bursty);

        stdout.println("stopped activity co_cycle_delay_bursty");
        return null;
    }
}

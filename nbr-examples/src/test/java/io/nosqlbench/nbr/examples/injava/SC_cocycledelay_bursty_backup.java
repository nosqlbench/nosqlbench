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

import io.nosqlbench.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenarioPhase;

import java.util.Map;

public class SC_cocycledelay_bursty_backup extends SCBaseScenarioPhase {
    public SC_cocycledelay_bursty_backup(NBComponent parentComponent, String scenarioName) {
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
    public void invoke() {
        var co_cycle_delay_bursty = Map.of(
            "alias", "co_cycle_delay_bursty",
            "driver", "diag",
            "cycles", "0..1000000",
            "threads", "1",
            "cyclerate", "1000,1.5",
            "op", "diagrate: diagrate=500"
        );


        stdout.println("starting activity co_cycle_delay_bursty");
        controller.start(co_cycle_delay_bursty);

        NBMetricCounter service_time_counter = find().counter("activity=co_cycle_delay_bursty,name=cycles_servicetime");
        NBMetricGauge wait_time_gauge = find().gauge("activity=co_cycle_delay_bursty,name=cycles_waittime");
        String diagrate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("diagrate").toString();
        String cyclerate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("cyclerate").toString();

        for (int i = 0; i < 5; i++) {
            controller.waitMillis(1000);
            if (!controller.isRunningActivity(co_cycle_delay_bursty)) {
                stdout.println("scenario exited prematurely, aborting.");
                break;
            }
            diagrate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("diagrate").toString();
            cyclerate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("cyclerate").toString();
            stdout.println(
                "backlogging, cycles=" + service_time_counter.getCount() +
                " waittime=" + wait_time_gauge.getValue() +
                " diagrate=" + diagrate +
                " cyclerate=" + cyclerate
            );
        }

        stdout.println("step1 metrics.waittime=" + wait_time_gauge.getValue());
        controller.getActivityDef("co_cycle_delay_bursty").getParams().put("diagrate", "10000");

        for (int i = 0; i < 10; i++) {
            if (!controller.isRunningActivity("co_cycle_delay_bursty")) {
                stdout.println("scenario exited prematurely, aborting.");
                break;
            }
            diagrate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("diagrate").toString();
            cyclerate = controller.getActivityDef("co_cycle_delay_bursty").getParams().get("cyclerate").toString();

            stdout.println(
                "recovering, cycles=" + service_time_counter.getCount() +
                " waittime=" + wait_time_gauge.getValue() +
                    " diagrate=" + diagrate +
                    " cyclerate=" + cyclerate
            );

            controller.waitMillis(1000);
            if (wait_time_gauge.getValue() < 50000000) {
                stdout.println("waittime trended back down as expected, exiting on iteration " + i);
                break;
            }
        }

        stdout.println("step2 metrics.waittime=" + wait_time_gauge.getValue());
        controller.stop(co_cycle_delay_bursty);

        stdout.println("stopped activity co_cycle_delay_bursty");


    }
}

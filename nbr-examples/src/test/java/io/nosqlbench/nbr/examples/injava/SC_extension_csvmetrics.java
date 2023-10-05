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
import io.nosqlbench.nbr.examples.SCBaseScenario;

public class SC_extension_csvmetrics extends SCBaseScenario {
    public SC_extension_csvmetrics(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * var csvlogger = csvmetrics.log("logs/csvmetricstestdir");
     *
     * activitydef = {
     *     "alias" : "csvmetrics",
     *     "driver" : "diag",
     *     "cycles" : "50000",
     *     "threads" : "1",
     *     "op": "log: level=debug",
     *     "rate" : "100.0"
     * };
     * scenario.start(activitydef);
     * scenario.waitMillis(500);
     *
     * csvlogger.add(metrics.csvmetrics.cycles_servicetime);
     * csvlogger.start(500,"MILLISECONDS");
     *
     * scenario.waitMillis(2000);
     * scenario.stop(activitydef);
     *
     * csvlogger.report();
     * }</pre>
     */
    @Override
    public void invoke() {
        // TODO create().csvmetrics....

    }
}

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

public class SC_histologger extends SCBaseScenario {
    public SC_histologger(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * activitydef = {
     *     "alias" : "testhistologger",
     *     "driver" : "diag",
     *     "cycles" : "1000",
     *     "threads" : "20",
     *     "interval" : "2000",
     *     "targetrate" : "100.0",
     *     "op" : "noop"
     * };
     *
     * histologger.logHistoIntervals("testing extention histologger", ".*", "hdrhistodata.log", "0.5s");
     * print("started logging to hdrhistodata.log for all metrics at 1/2 second intervals.");
     *
     * scenario.start(activitydef);
     * scenario.waitMillis(3000);
     * scenario.stop(activitydef);
     * }</pre>
     */
    @Override
    public void invoke() {

    }
}

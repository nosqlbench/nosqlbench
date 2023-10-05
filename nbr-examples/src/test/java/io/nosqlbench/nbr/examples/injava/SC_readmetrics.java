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

public class SC_readmetrics extends SCBaseScenario {
    public SC_readmetrics(NBComponent parentComponent, String scenarioName) {
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
    public void invoke() {

    }
}

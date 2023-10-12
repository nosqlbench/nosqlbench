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
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import io.nosqlbench.engine.extensions.example.ExamplePlugin;

public class SC_extension_example extends SCBaseScenario {
    public SC_extension_example(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * var csvlogger = csvoutput.open("logs/csvoutputtestfile.csv","header1","header2");
     *
     * csvlogger.write({"header1": "value1","header2":"value2"});
     * }</pre>
     */
    @Override
    public void invoke() {
        ExamplePlugin examplePlugin = create().requiredExtension("example", ExamplePlugin.class);
        long sum = examplePlugin.getSum(3, 5);
        stdout.println("3+5=" + sum);
    }
}

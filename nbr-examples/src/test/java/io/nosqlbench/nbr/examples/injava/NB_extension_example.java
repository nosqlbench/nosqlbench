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

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;

import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.extensions.example.ExamplePlugin;

import java.io.PrintWriter;
import java.io.Reader;

public class NB_extension_example extends NBBaseCommand {
    public NB_extension_example(NBBufferedCommandContext parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * var csvlogger = csvoutput.open("logs/csvoutputtestfile.csv","header1","header2");
     *
     * csvlogger.write({"header1": "value1","header2":"value2"});
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        ExamplePlugin examplePlugin = create().requiredExtension("example", ExamplePlugin.class);
        long sum = examplePlugin.getSum(3, 5);
        stdout.println("3+5=" + sum);
        return null;
    }
}

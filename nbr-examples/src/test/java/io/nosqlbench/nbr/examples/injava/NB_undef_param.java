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


import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NB_undef_param extends NBBaseCommand {
    public NB_undef_param(NBBufferedCommandContext parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * print("paramValues from command line:");
     * print(paramValues);
     * print('before: paramValues["three"]:' + paramValues["three"]);
     * print('before: paramValues.three:' + paramValues.three);
     *
     * var overrides = {
     *   'three': "undef",
     * };
     *
     * print("paramValues.three after overriding with three:UNDEF");
     * paramValues = paramValues.withOverrides({'three':'UNDEF'});
     * print(paramValues);
     * print('after: paramValues["three"]:' + paramValues["three"]);
     * print('after: paramValues.three:' + paramValues.three);
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        stdout.println("paramValues from command line:");
        stdout.println(params.toString());
        stdout.println("before: paramValues.get(\"three\"):" + params.get("three"));
        var overrides = Map.of(
            "three", "undef"
        );
        params=params.withOverrides(Map.of(
            "three","UNDEF"
        ));
        stdout.println("after overriding with three:UNDEF: paramValues.get(\"three\"):" + params.get("three"));
        return null;
    }
}

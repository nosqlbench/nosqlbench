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

import java.util.Map;

public class SC_undef_param extends SCBaseScenario {
    public SC_undef_param(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * print("params from command line:");
     * print(params);
     * print('before: params["three"]:' + params["three"]);
     * print('before: params.three:' + params.three);
     *
     * var overrides = {
     *   'three': "undef",
     * };
     *
     * print("params.three after overriding with three:UNDEF");
     * params = params.withOverrides({'three':'UNDEF'});
     * print(params);
     * print('after: params["three"]:' + params["three"]);
     * print('after: params.three:' + params.three);
     * }</pre>
     */
    @Override
    public void invoke() {
        stdout.println("params from command line:");
        stdout.println(params.toString());
        stdout.println("before: params.get(\"three\"):" + params.get("three"));
        var overrides = Map.of(
            "three", "undef"
        );
        params=params.withOverrides(Map.of(
            "three","UNDEF"
        ));
        stdout.println("after overriding with three:UNDEF: params.get(\"three\"):" + params.get("three"));
    }
}

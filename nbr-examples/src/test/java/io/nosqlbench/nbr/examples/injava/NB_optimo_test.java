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
import io.nosqlbench.nb.api.optimizers.BobyqaOptimizerInstance;
import io.nosqlbench.nb.api.optimizers.MVResult;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;


import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.ToDoubleFunction;

public class NB_optimo_test extends NBBaseCommand {
    public NB_optimo_test(NBBufferedCommandContext parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    /** <pre>{@code
     * var optimo = optimos.init();
     *
     * optimo.param('pa', 0.0, 200000.0);
     * optimo.param('pb', 0.0, 200000.0);
     *
     * optimo.setInitialRadius(10000.0).setStoppingRadius(0.001).setMaxEval(1000);
     *
     * optimo.setObjectiveFunction(
     *     function (values) {
     *         // var arraydata = Java.from(ary);
     *         print("ary:" + JSON.stringify(values));
     *
     *         var a = 0.0 + values.pa;
     *         var b = 0.0 + values.pb;
     *
     *         var samples = 1000000 - ((Math.abs(100 - a) + Math.abs(100 - b)));
     *         print("a=" + a + ",b=" + b + ", r=" + samples);
     *         return samples;
     *     }
     * );
     *
     * var samples = optimo.optimize();
     *
     * print("optimized samples was " + samples);
     * print("map of samples was " + samples.getMap());
     * }</pre>
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        BobyqaOptimizerInstance bobby = create().bobyqaOptimizer();
        bobby.param("pa", 0.0d, 200000.0d);
        bobby.param("pb", 0.0d, 2000000d);

        bobby.setInitialRadius(10000.0).setStoppingRadius(0.001).setMaxEval(1000);

        ToDoubleFunction<double[]> f = new ToDoubleFunction<double[]>() {
            @Override
            public double applyAsDouble(double[] value) {
                return 10000000 - ((Math.abs(100-value[0])) + (Math.abs(100-value[1])));
            }
        };
        bobby.setObjectiveFunction(f);
        MVResult result = bobby.optimize();
        stdout.println("optimized samples was " + result);
        stdout.println("map of samples was " + result.getMap());
        return null;
    }
}

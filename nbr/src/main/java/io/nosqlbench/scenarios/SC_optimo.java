package io.nosqlbench.scenarios;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.optimizers.BobyqaOptimizerInstance;
import io.nosqlbench.api.optimizers.MVResult;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class SC_optimo extends SCBaseScenario {
    private final static Logger logger = LogManager.getLogger(SC_optimo.class);
    public SC_optimo(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    @Override
    public void invoke() {
        // TODO: having "scenario" here as well as in "named scenario" in workload templates is confusing. Make this clearer.
        String workload = params.getOrDefault("workload", "default_workload");

        Map<String,String> activityParams = new HashMap<>(Map.of(
            "cycles", String.valueOf(Long.MAX_VALUE),
            "threads", "1",
            "driver", "diag",
            "rate", "1"
        ));
        if (params.containsKey("workload")) {
            activityParams.put("workload",params.get("workload"));
        } else if (params.containsKey("op")) {
            activityParams.put("op",params.get("op"));
        } else {
            activityParams.put("op","log: level=info");
            logger.warn("You provided neither a workload nor an op, so assuming diagnostic mode.");
        }

        Activity flywheel = controller.start(activityParams);

        BobyqaOptimizerInstance bobby = create().bobyqaOptimizer();

        bobby.param("threads", 0.0d, 200000.0d);
        bobby.param("rate", 0.0d, 1_000_000.d);
        bobby.setInitialRadius(10000.0).setStoppingRadius(0.001).setMaxEval(1000);

        /**
         * <P>This function is the objective function, and is responsible for applying
         * the parameters and yielding a result. The higher the returned result, the
         * better the parameters are.</P>
         * <P>The parameter values will be passed in as an array, pair-wise with the param calls above.</P>
         */
        ToDoubleFunction<double[]> f = new ToDoubleFunction<double[]>() {
            @Override
            public double applyAsDouble(double[] value) {
                int threads=(int)value[0];

                NBMetric counter = flywheel.find().counter("counterstuff");

                flywheel.getActivityDef().setThreads(threads);

                double rate=value[1];
                flywheel.getActivityDef().setCycles(String.valueOf(rate));


                return 10000000 - ((Math.abs(100-value[0])) + (Math.abs(100-value[1])));
            }
        };
        bobby.setObjectiveFunction(f);
        MVResult result = bobby.optimize();
        stdout.println("optimized result was " + result);
        stdout.println("map of result was " + result.getMap());

        // TODO: controller start should not return the activity itself, but a control point, like activityDef

        // TODO: warn user if one of the result params is near or at the range allowed, as there
        // could be a better result if the range is arbitrarily limiting the parameter space.
    }
}

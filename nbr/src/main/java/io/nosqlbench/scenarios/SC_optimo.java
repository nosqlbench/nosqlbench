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


import io.nosqlbench.api.engine.metrics.ConvenientSnapshot;
import io.nosqlbench.api.engine.metrics.DeltaSnapshotReader;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.api.optimizers.BobyqaOptimizerInstance;
import io.nosqlbench.api.optimizers.MVResult;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.events.ParamChange;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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

        Map<String, String> activityParams = new HashMap<>(Map.of(
            "cycles", String.valueOf(Long.MAX_VALUE),
            "threads", "1",
            "driver", "diag",
            "rate", "1",
            "dryrun","op"
        ));
        if (params.containsKey("workload")) {
            activityParams.put("workload", params.get("workload"));
        } else if (params.containsKey("op")) {
            activityParams.put("op", params.get("op"));
        } else {
            activityParams.put("op", "log: level=info");
            logger.warn("You provided neither a workload nor an op, so assuming diagnostic mode.");
        }

        int seconds = params.containsKey("window") ? Integer.parseInt(params.get("window")) : 5;

        BobyqaOptimizerInstance bobby = create().bobyqaOptimizer();
        bobby.param("rate", 1.0d, 10000.d);
        bobby.param("threads", 1.0d, 1000.0d);
//        bobby.param("noise", 100d, 200.0d);
        bobby.setInitialRadius(1000000.0).setStoppingRadius(0.001).setMaxEval(1000);

        Activity flywheel = controller.start(activityParams);
        stdout.println("warming up for " + seconds + " seconds");
        controller.waitMillis(5000);

        /**
         * <P>This function is the objective function, and is responsible for applying
         * the parameters and yielding a result. The higher the returned result, the
         * better the parameters are.</P>
         * <P>The parameter values will be passed in as an array, pair-wise with the param calls above.</P>
         */

        flywheel.onEvent(new ParamChange<>(new CycleRateSpec(5.0, 1.1d, SimRateSpec.Verb.restart)));

        PerfWindowSampler sampler = new PerfWindowSampler();
        NBMetricTimer result_success_timer = flywheel.find().timer("name:result_success");
        System.out.println("c1:" + result_success_timer.getCount());
        sampler.addDeltaTime("achieved_rate", result_success_timer::getCount, 1000.0);
        System.out.println("c2:" + result_success_timer.getCount());
        stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());
        final DeltaSnapshotReader snapshotter = result_success_timer.getDeltaReader();
        AtomicReference<ConvenientSnapshot> snapshot = new AtomicReference<>(snapshotter.getDeltaSnapshot());
//        ValidAtOrBelow below15000 = ValidAtOrBelow.max(15000);
//        sampler.addDirect(
//            "p99latency",
//            () -> below15000.applyAsDouble(snapshot.get().getP99ns()),
//            -1.0,
//            () -> snapshot.set(snapshotter.getDeltaSnapshot())
//        );

        ToDoubleFunction<double[]> f = new ToDoubleFunction<double[]>() {
            @Override
            public double applyAsDouble(double[] values) {
                stdout.println("params=" + Arrays.toString(values));

                System.out.println("c3:" + result_success_timer.getCount());
                stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());

                int threads = (int) bobby.getParams().getValue("threads", values);
                flywheel.getActivityDef().setThreads(threads);
                stdout.println("PARAM threads set to " + threads + " confirm: " + flywheel.getActivityDef().getThreads());

                double rate = bobby.getParams().getValue("rate", values);
                CycleRateSpec ratespec = new CycleRateSpec(rate, 1.1d, SimRateSpec.Verb.restart);
                flywheel.onEvent(new ParamChange<>(ratespec));
                stdout.println("PARAM cyclerate set to " +rate);
                stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());

                System.out.println("c3b:" + result_success_timer.getCount());
                stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());
                stdout.println("waiting 2 seconds for stabilization");
                controller.waitMillis(2000);

                System.out.println("c4:" + result_success_timer.getCount());
                stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());

                sampler.startWindow();
                stdout.println("waiting " + seconds + " seconds...");
                controller.waitMillis(seconds * 1000L);
                sampler.stopWindow();
                System.out.println("c5:" + result_success_timer.getCount());
                stdout.println(" RATE>>> " + flywheel.getCycleLimiter().toString());

                double value = sampler.getValue();
                stdout.println("RESULT:\n" + sampler);
                stdout.println("-".repeat(40));
                return value;

            }
        };
        bobby.setObjectiveFunction(f);
        MVResult result = bobby.optimize();

        controller.stop(flywheel);
        stdout.println("optimized result was " + result);
        stdout.println("map of result was " + result.getMap());

        // TODO: controller startAt should not return the activity itself, but a control point, like activityDef

        // TODO: warn user if one of the result params is near or at the range allowed, as there
        // could be a better result if the range is arbitrarily limiting the parameter space.
    }
}

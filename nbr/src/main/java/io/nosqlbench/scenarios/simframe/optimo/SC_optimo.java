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

package io.nosqlbench.scenarios.simframe.optimo;

import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.events.ParamChange;
import io.nosqlbench.components.events.SetThreads;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.findmax.SC_findmax;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;
import org.apache.commons.math4.legacy.exception.MathIllegalStateException;
import org.apache.commons.math4.legacy.optim.*;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SC_optimo extends SCBaseScenario {
    private final static Logger logger = LogManager.getLogger(SC_findmax.class);

    public SC_optimo(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    @Override
    public void invoke() {
        // TODO: having "scenario" here as well as in "named scenario" in workload templates is confusing. Make this clearer.
        String workload = params.getOrDefault("workload", "default_workload");
        CycleRateSpec ratespec = new CycleRateSpec(100.0, 1.05);

        Map<String, String> activityParams = new HashMap<>(Map.of(
            "cycles", String.valueOf(Long.MAX_VALUE),
            "threads", params.getOrDefault("threads", "1"),
            "driver", "diag",
            "rate", String.valueOf(ratespec.opsPerSec),
            "dryrun", "op"
        ));
        if (params.containsKey("workload")) {
            activityParams.put("workload", params.get("workload"));
        } else if (params.containsKey("op")) {
            activityParams.put("op", params.get("op"));
        } else {
            activityParams.put("op", "log: level=info");
            logger.warn("You provided neither a workload nor an op, so assuming diagnostic mode.");
        }

        Activity flywheel = controller.start(activityParams);
        SimFrameCapture capture = this.perfValueMeasures(flywheel);
        SimFrameJournal<OptimoFrameParams> journal = new SimFrameJournal<>();


        OptimoParamModel model = new OptimoParamModel();

        model.add("rate",100,1000,10000000,
            rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(rate, 1.1d, SimRateSpec.Verb.restart)))
        );
        model.add("threads",1,10,10000,
            threads -> flywheel.onEvent(ParamChange.of(new SetThreads((int) threads)))
        );
        OptimoSearchSettings optimoSearchParams = new OptimoSearchSettings(params,model);
        SimFrameFunction frameFunction = new OptimoFrameFunction(controller,optimoSearchParams,flywheel,capture,journal);


        List<OptimizationData> od = List.of(
            new ObjectiveFunction(frameFunction),
            GoalType.MAXIMIZE,
            new InitialGuess(model.getInitialGuess()),
            new MaxEval(2500),
            model.getBounds()
        );

        /**
         * optimizer settings
         * <PRE>{@code
         * ΔT  IP  ITR  STR    ITER WINNER P
         * 4S  6   10   1E-3   101  70     rate=1807028.66 threads=5479.26
         * 4S  6   10   1E-3   25   12     rate=1010.55    threads=1.78
         * 4S  6   10   1E-3   37   32     rate=656537.93  threads=1.0
         * 4S  6   25   1E-5   67   41     rate=2278757.14 threads=4767
         * 4S  6   25   1E-5   91   62     rate=2477244.22 threads=8905.10
         * 4S  6   25   1E-5   86   74     rate=2339949.66 threads=4796.69
         * 4S  6   100  1E-4   77   39     rate=1048884.14 threads=1.0
         * 4S  6   25   1E-4   32   32     rate=1674547.43 threads=1.0
         * 4S  6   10   1E-4   95   64     rate=1796326.69 threads=1.0
         * 4S  6   25   1E-4   74   59     rate=2327131.37 threads=10000
         * 4S  6   25   1E-4   93   78     rate=2422970.28 threads=2435.21
         * 4S  6   25   1E-3   33   27     rate=1738588.36 threads=1.0
         * 4S  6   5    1E-4   87   86     rate=1603980.57 threads=38.12
         * 4S  6   5    1E-5   32   0      rate=1000 threads=10 (stuck, and again)
         * 4S  6   15   1E-5   33   27     rate=1340405.42 threads=1.11 // these runs seem to be suffering from over-saturation hang-over
         * 4S  6   50   1E-5   72   71     rate=952825.94  threads=434  // same as above, stabilization checks must be implemented
         * </PRE>
         *
         * If the initial trust radius is not large enough with respect to the stopping trust radius, then the search will stop
         * <I>ascending</I> the approximate manifold too early. This seems to be because the trust radius is forced to diminish
         * by some limiting condition.
         */
        BOBYQAOptimizer mo = new BOBYQAOptimizer(
            /**
             * Interpolation points, limited by the dimensionality of the parameter vector.
             * M.J.D. Powell recommends this be 2n+1, where n is the number of parameters in the search vector
             */
            6,
            /**
             * Initial trust region radius, affects how well aggressively BOBYQA explores the parameter space.
             * Too small and it may not explore enough to find a gradient. Too large, and the feedback loop seems to run dry quickly.
             */
            50,
            /**
             * Stopping trust region radius. Differences in result which are below this value are likely to stop the search
             */
            1E-5
        );
        PointValuePair result = null;
        try {
            result = mo.optimize(od.toArray(new OptimizationData[od.size()]));
        } catch (MathIllegalStateException missed) {
            if (missed.getMessage().contains("trust region step has failed to reduce Q")) {
                logger.warn(missed.getMessage()+", so returning current result.");
                result = new PointValuePair(journal.last().params().paramValues(), journal.last().value());
            } else {
                throw missed;
            }
        }
        stdout.println("result:" + result);

        controller.stop(flywheel);
        stdout.println("bestrun:\n" + journal.bestRun());

        // could be a better result if the range is arbitrarily limiting the parameter space.
    }

    private SimFrameCapture perfValueMeasures(Activity activity) {
        SimFrameCapture sampler = new SimFrameCapture();

        NBMetricTimer result_timer = activity.find().timer("name:result");
        NBMetricTimer result_success_timer = activity.find().timer("name:result_success");
        NBMetricGauge cyclerate_gauge = activity.find().gauge("name=config_cyclerate");
        NBMetricHistogram tries_histo_src = activity.find().histogram("name=tries");
        NBMetricHistogram tries_histo = tries_histo_src.attachHdrDeltaHistogram();

        sampler.addDirect("target_rate", cyclerate_gauge::getValue, Double.NaN);
        sampler.addDeltaTime("achieved_oprate", result_timer::getCount, Double.NaN);
        sampler.addDeltaTime("achieved_ok_oprate", result_success_timer::getCount, 1.0);

        sampler.addRemix("achieved_success_ratio", vars -> {
            // exponentially penalize results which do not attain 100% successful op rate
            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("achieved_oprate"));
            return Math.pow(basis, 2);
        });
        sampler.addRemix("achieved_target_ratio", (vars) -> {
            // exponentially penalize results which do not attain 100% target rate
            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("target_rate"));
            return Math.pow(basis, 2);
        });
//        sampler.addRemix("retries_p99", (vars) -> {
//            double retriesP99 = tries_histo.getDeltaSnapshot(90).get99thPercentile();
//            return 1/retriesP99;
//        });


        return sampler;
    }
}

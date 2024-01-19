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

package io.nosqlbench.scenarios.simframe.optimizers.optimo;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.optimizers.CMD_optimize;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;
import io.nosqlbench.scenarios.simframe.stabilization.StatFunctions;
import org.apache.commons.math4.legacy.exception.MathIllegalStateException;
import org.apache.commons.math4.legacy.optim.*;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

/**
 * If the initial trust radius is not large enough with respect to the stopping trust radius, then the search will stop
 * <I>ascending</I> the approximate manifold too early. This seems to be because the trust radius is forced to diminish
 * by some limiting condition.
 * <HR/>
 * <H2>Explanation of parameters:</H2>
 * <OL>
 *     <LI>numberOfInterpolationPoints - limited by the dimensionality of the parameter vector.
 *          M.J.D. Powell recommends this be 2n+1, where n is the number of parameters in the search vector.</LI>
 *     <LI>initialTrustRegionRadius - Initial trust region radius, affects how well aggressively BOBYQA explores the parameter space.
 *          Too small and it may not explore enough to find a gradient if it starts in a flatter part of the manifold.
 *          Too large, and the feedback loop seems to run dry quickly due to lack of detail.</LI>
 *     <LI>stoppingTrustRegionRadius - Stopping trust region radius. This is the space which contains interpolation points, and can be trusted
 *          to contain a reasonably representative character of the overall manifold</LI>
 * </OL>
 */
public class CMD_optimo extends NBBaseCommand {
    private final static Logger logger = LogManager.getLogger(CMD_optimize.class);

    public CMD_optimo(NBBufferedContainer parentComponent, String phaseName, String targetScenario) {
        super(parentComponent, phaseName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        // TODO: having "scenario" here as well as in "named scenario" in workload templates is confusing. Make this clearer.

        Activity flywheel = SimFrameUtils.findFlywheelActivity(controller, params.get("activity"));
        stdout.println("starting analysis on activity '" + flywheel.getAlias() + "'");
        SimFrameUtils.awaitActivity(flywheel);

        SimFrameJournal<OptimoFrameParams> journal = new SimFrameJournal<>();
        OptimoParamModel model = new OptimoParamModel();

        OptimoSearchSettings optimoSearchParams = new OptimoSearchSettings(params, model);

        model.add("rate", 10, optimoSearchParams.startRate(), optimoSearchParams.startRate()*4,
            rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(rate, 1.1d, SimRateSpec.Verb.restart)))
        );
        model.add("threads", 10, 50, 2000,
            threads -> flywheel.onEvent(ParamChange.of(new SetThreads((int) (threads))))
        );

        SimFrameCapture capture = this.perfValueMeasures(flywheel, optimoSearchParams);
        SimFrameFunction frameFunction = new OptimoFrameFunction(controller, optimoSearchParams, flywheel, capture, journal);

        List<OptimizationData> od = List.of(
            new ObjectiveFunction(frameFunction),
            GoalType.MAXIMIZE,
            new InitialGuess(model.getInitialGuess()),
            new MaxEval(100),
            model.getBounds()
        );

        BOBYQAOptimizer mo = new BOBYQAOptimizer(
            6,
            25,
            1E-4
        );
        PointValuePair result = null;
        try {
            result = mo.optimize(od.toArray(new OptimizationData[0]));
        } catch (MathIllegalStateException missed) {
            if (missed.getMessage().contains("trust region step has failed to reduce Q")) {
                logger.warn(missed.getMessage() + ", so returning current result.");
                result = new PointValuePair(journal.last().params().paramValues(), journal.last().value());
            } else {
                throw missed;
            }
        }
        stdout.println("result:" + result);

        SimFrame<OptimoFrameParams> best = journal.bestRun();
        stdout.println("bestrun:\n" + best);
        return best.params();
        // could be a better result if the range is arbitrarily limiting the parameter space.
    }

    private SimFrameCapture perfValueMeasures(Activity activity, OptimoSearchSettings settings) {
        SimFrameCapture sampler = new SimFrameCapture();

        NBMetricTimer result_timer = activity.find().timer("name:result");
        NBMetricTimer latency_histo = result_timer.attachHdrDeltaHistogram();

        NBMetricTimer result_success_timer = activity.find().timer("name:result_success");
        NBMetricGauge cyclerate_gauge = activity.find().gauge("name=config_cyclerate");
        NBMetricHistogram tries_histo_src = activity.find().histogram("name=tries");
        NBMetricHistogram tries_histo = tries_histo_src.attachHdrDeltaHistogram();

        sampler.addDirect("target_rate", cyclerate_gauge::getValue, Double.NaN);
        sampler.addDeltaTime("achieved_oprate", result_timer::getCount, Double.NaN);
        sampler.addDeltaTime("achieved_ok_oprate", result_success_timer::getCount, 1.0);

//        sampler.addRemix("achieved_success_ratio", vars -> {
//            // exponentially penalize results which do not attain 100% successful op rate
//            if (vars.get("achieved_oprate") == 0.0d) {
//                return 0d;
//            }
//            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("achieved_oprate"));
//            return basis;
////            return Math.pow(basis, 2);
//        });

//        sampler.addRemix("achieved_target_ratio", (vars) -> {
//            // exponentially penalize results which do not attain 100% target rate
//            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("target_rate"));
//            return basis;
////            return Math.pow(basis, 2);
//        });
        sampler.addRemix("retries_p99", (vars) -> {
            double triesP99 = tries_histo.getDeltaSnapshot(90).get99thPercentile();
            if (Double.isNaN(triesP99) || Double.isInfinite(triesP99) || triesP99 == 0.0d) {
                // There wasn't enough data in the histogram to make a call one way or another,
                // so this won't really be a factor
//                logger.warn("invalid value for retries_p99, skipping as identity for now");
                return 1.0d;
            }
            return 1 / triesP99;
        });
        sampler.addDirect("latency_cutoff_50", () -> {
            double latencyP99 = (latency_histo.getDeltaSnapshot(90).getValue(settings.cutoff_quantile())) / 1_000_000d;
            double v = (StatFunctions.sigmoidE4LowPass(latencyP99, settings.cutoff_ms()));
//            System.out.println("v:"+v+"  p99ms:" + latencyP99 + " cutoff_quantile=" + settings.cutoff_quantile() + " cutoff_ms=" + settings.cutoff_ms());
            return v;
//            return 1.0d;
        }, 1.0d);
        return sampler;
    }
}

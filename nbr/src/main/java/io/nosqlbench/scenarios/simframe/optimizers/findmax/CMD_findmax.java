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

package io.nosqlbench.scenarios.simframe.optimizers.findmax;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.optimizers.CMD_optimize;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;
import io.nosqlbench.scenarios.simframe.stabilization.StatFunctions;
import org.apache.commons.math4.legacy.exception.MathIllegalStateException;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.OptimizationData;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

public class CMD_findmax extends NBBaseCommand {
    private final static Logger logger = LogManager.getLogger(CMD_optimize.class);

    public CMD_findmax(NBBufferedContainer parentComponent, String phaseName, String targetScenario) {
        super(parentComponent, phaseName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Activity flywheel = SimFrameUtils.findFlywheelActivity(controller, params.get("activity"));
        stdout.println("starting analysis on activity '" + flywheel.getAlias() + "'");
        SimFrameUtils.awaitActivity(flywheel);

        /*
        var frameParams = initialStep();

        while (frameParams != null) {
            stdout.println(frameParams);
            applyParams(frameParams,flywheel);
            capture.startWindow();
            if (this instanceof HoldAndSample has) {
                has.holdAndSample(capture);
            } else {
                capture.awaitSteadyState();
            }
            capture.stopWindow();
            journal.record(frameParams, capture.last());
            stdout.println(capture.last());
            stdout.println("-".repeat(40));
            frameParams = nextStep(journal);
        }
        return journal.bestRun().params();

         */
        SimFrameJournal<FindmaxFrameParams> journal = new SimFrameJournal<>();
        FindmaxParamModel model = new FindmaxParamModel();

        FindmaxSearchSettings findmaxSearchParams = new FindmaxSearchSettings(params, model);

//  initial step:
//        new io.nosqlbench.scenarios.simframe.optimizers.planners.findmax.FindmaxFrameParams(
//            findmaxSearchParams.rate_base(),
//            findmaxSearchParams.rate_step(),
//            findmaxSearchParams.sample_time_ms(),
//            findmaxSearchParams.min_settling_ms(),
//            "INITIAL"
//        );

        model.add("rate", 10, findmaxSearchParams.rate_base(), findmaxSearchParams.rate_base()*10,
            rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(rate, 1.1d, SimRateSpec.Verb.restart)))
            //rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate_shelf()+params.rate_delta(), 1.1d, SimRateSpec.Verb.restart)));
        );

        SimFrameCapture capture = this.perfValueMeasures(flywheel, findmaxSearchParams);
        SimFrameFunction frameFunction = new FindmaxFrameFunction(controller, findmaxSearchParams, flywheel, capture, journal);

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

        SimFrame<FindmaxFrameParams> best = journal.bestRun();
        stdout.println("bestrun:\n" + best);
        return best.params();
        // could be a better result if the range is arbitrarily limiting the parameter space.
    }

    private SimFrameCapture perfValueMeasures(Activity activity, FindmaxSearchSettings settings) {
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

        sampler.addRemix("retries_p99", (vars) -> {
            double triesP99 = tries_histo.getDeltaSnapshot(90).get99thPercentile();
            if (Double.isNaN(triesP99) || Double.isInfinite(triesP99) || triesP99 == 0.0d) {
                return 1.0d;
            }
            return 1 / triesP99;
        });
//        sampler.addDirect("latency_cutoff_50", () -> {
//            double latencyP99 = (latency_histo.getDeltaSnapshot(90).getValue(settings.cutoff_quantile())) / 1_000_000d;
//            double v = (StatFunctions.sigmoidE4LowPass(latencyP99, settings.cutoff_ms()));
//            return v;
//        }, 1.0d);
        return sampler;
    }
}

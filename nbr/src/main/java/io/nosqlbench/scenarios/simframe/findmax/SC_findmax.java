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

package io.nosqlbench.scenarios.simframe.findmax;

import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.events.ParamChange;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <P>This is the Java-native version of findmax on the NB5.21 architecture. It has been modified from the
 * previous form in significant ways.</P>
 * <UL>
 * <LI>Instead of pass/fail criteria all performance factors are expressed as a value function.
 * This will make it easy to adapt different search algorithms (in the form of non-derivative multivariate optimizers)
 * to the same approach.</LI>
 * <LI>A set of simulation frame utilities bundles the derivation of black-box values. These will be used to refine
 * findmax, but will
 * be general-purposed for any other analysis and optimization method as needed.
 * </LI>
 * <LI>The search strategy which determines the parameters for the next simulation frame has been factored out into a
 * planner.</LI>
 * </UL>
 *
 * <P>There is an accompanying visual narrative "findmax.png" bundled with this source code to help explain
 * the search pattern of findmax. Additional docs and a usage guide will follow.</P>
 *
 * <P>This can be tested as <PRE>{@code nb5 --show-stacktraces java io.nosqlbench.scenarios.findmax.SC_findmax threads=36}</PRE></P>
 */
public class SC_findmax extends SCBaseScenario {
    private final static Logger logger = LogManager.getLogger(SC_findmax.class);

    public SC_findmax(NBComponent parentComponent, String scenarioName) {
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

        FindmaxSearchParams findmaxSettings = new FindmaxSearchParams(params);

        Activity flywheel = controller.start(activityParams);

        SimFrameCapture capture = this.perfValueMeasures(flywheel);
        FindMaxPlanner planner = new FindMaxPlanner(findmaxSettings);
        SimFrameJournal journal = new SimFrameJournal();

        FindMaxFrameParams frameParams = planner.initialStep();
        while (frameParams != null) {
            stdout.println(frameParams);
//            flywheel.onEvent(ParamChange.of(new CycleRateSpec(frameParams.computed_rate(), 1.05d, SimRateSpec.Verb.restart)));
//            long settling_time = frameParams.settling_time_ms();
//            if (settling_time>0) {
//                stdout.println("settling for " + settling_time + " ms");
//                controller.waitMillis(settling_time);
//            }
            flywheel.onEvent(ParamChange.of(new CycleRateSpec(frameParams.computed_rate(), 1.05d, SimRateSpec.Verb.restart)));
            capture.startWindow();
            for (int i = 0; i < 10; i++) {
                controller.waitMillis(frameParams.sample_time_ms()/10);
                stdout.println(capture.activeSample());
            }
//            controller.waitMillis(frameParams.sample_time_ms());
//            capture.awaitSteadyState();
//            capture.restartWindow();
//            flywheel.onEvent(ParamChange.of(new CycleRateSpec(frameParams.computed_rate(), 1.05d, SimRateSpec.Verb.restart)));
//            controller.waitMillis(frameParams.sample_time_ms());
            capture.stopWindow();
            journal.record(frameParams, capture.last());
            stdout.println(capture.last());
            stdout.println("-".repeat(40));
            frameParams = planner.nextStep(journal);
        }
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
            return Math.pow(basis, 3);
        });
        sampler.addRemix("achieved_target_ratio", (vars) -> {
            // exponentially penalize results which do not attain 100% target rate
            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("target_rate"));
            return Math.pow(basis, 3);
        });
//        sampler.addRemix("retries_p99", (vars) -> {
//            double retriesP99 = tries_histo.getDeltaSnapshot(90).get99thPercentile();
//            return 1/retriesP99;
//        });


        return sampler;
    }
}

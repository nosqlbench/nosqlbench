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

package io.nosqlbench.scenarios.findmax;

import io.nosqlbench.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.events.ParamChange;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.direct.SCBaseScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <PRE>{@code
 * Performing FindMax analysis with the following parameters:
 * Scale sample window between %d and %d\n",sample_time_ms,sample_max
 * increasing by %.3fX on each rejected iteration.\n", sample_incr
 * Set target rate to %d + %d  * ( %d ^iter) \n", rate_base, rate_step, rate_incr
 * for each iteration accepted in a row.\n"
 * Schedule %s operations at a time per thread.\n", min_stride
 * Report the average result of running the findmax search algorithm %d times.\n",averageof
 * Reject iterations which fail to achieve %2.0f%% of the target rate.\n", testrate_cutoff * 100
 * Reject iterations which fail to achieve %2.0f%% of the best rate.\n", bestrate_cutoff * 100
 * Reject iterations which fail to achieve better than %dms response\n", latency_cutoff
 * at percentile p%f\n", latency_pctile * 100
 * }</PRE>
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

        Map<String, String> activityParams = new HashMap<>(Map.of(
            "cycles", String.valueOf(Long.MAX_VALUE),
            "threads", "1",
            "driver", "diag",
            "rate", "1",
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

        int seconds = findmaxSettings.sample_time_ms();
//        double target_rate = findmaxSettings.rate_base() + findmaxSettings.rate_step();

        Activity flywheel = controller.start(activityParams);
        final double[] target_rate = new double[] {findmaxSettings.rate_step()};
        NBFunctionGauge targetRateGauge = flywheel.create().gauge("target_rate", () -> target_rate[0]);
//        stdout.println("warming up for " + seconds + " seconds");
//        controller.waitMillis(seconds * 1000);

        SimFrameCapture capture = this.perfValueMeasures(flywheel, 0.99, 50);
        SimFramePlanner planner = new SimFramePlanner(findmaxSettings);
        SimFrameJournal journal = new SimFrameJournal();

        SimFrameParams frameParams = planner.initialStep();
        while (frameParams!=null) {
            stdout.println("params:" + frameParams);
            target_rate[0] = frameParams.computed_rate();
            flywheel.onEvent(ParamChange.of(new CycleRateSpec(target_rate[0], 1.05d, SimRateSpec.Verb.restart)));
            capture.startWindow();
            controller.waitMillis(frameParams.sample_time_ms());
            capture.stopWindow();
            journal.record(frameParams,capture.last());
            stdout.println(capture.last());
//            stdout.println("SUMMARY:" + journal.last());
            stdout.println("-".repeat(40));
            frameParams = planner.nextStep(journal);
        }
        controller.stop(flywheel);

        // could be a better result if the range is arbitrarily limiting the parameter space.
    }

    private SimFrameCapture perfValueMeasures(Activity activity, double fractional_quantile, double cutoff_ms) {
        SimFrameCapture sampler = new SimFrameCapture();

        NBMetricTimer result_success_timer = activity.find().timer("name:result_success");
        NBMetricTimer result_timer = activity.find().timer("name:result");

        // achieved rate
        sampler.addDeltaTime(
            "achieved_rate",
            result_success_timer::getCount,
            1.0
        );

//        NBMetricGauge target_rate_gauge = activity.find().gauge("name=target_rate");
//        sampler.addDirect(
//            "achieved_ratio",
//            () -> Math.max(1.0d,(result_success_timer.getCount() / target_rate_gauge.getValue()))*Math.max(1.0d,(result_success_timer.getCount() / target_rate_gauge.getValue())),
//            1.0
//        );

//        // response time
//        sampler.addDirect(
//            "latency",
//            () -> {
//                double quantile_response_ns = result_success_timer.getDeltaSnapshot(1000).getValue(fractional_quantile);
//                if (quantile_response_ns * 1000000 > cutoff_ms) {
//                    return 0.0d;
//                } else {
//                    return quantile_response_ns;
//                }
//            },
//            -1
//        );
//
//        // error count
//        sampler.addDeltaTime(
//            "error_rate",
//            () -> result_timer.getCount() - result_success_timer.getCount(),
//            -1
//        );
//

        return sampler;
    }

    private final static record RunParams(
        int sample_seconds,
        double target_rate
    ) {
    }

}

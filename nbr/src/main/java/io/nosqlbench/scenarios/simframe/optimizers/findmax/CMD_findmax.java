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
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.capture.SimFrameValueData;
import io.nosqlbench.scenarios.simframe.optimizers.CMD_optimize;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;
import org.apache.commons.math4.legacy.exception.MathIllegalStateException;
import org.apache.commons.math4.legacy.optim.OptimizationData;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;

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

        SimFrameJournal<FindmaxFrameParams> journal = new SimFrameJournal<>();
        FindmaxParamModel model = new FindmaxParamModel();

        FindmaxConfig findmaxConfig = new FindmaxConfig(params);

        model.add("rate",
            findmaxConfig.rate_base(),    // min
            findmaxConfig.rate_base(),    // initial
            findmaxConfig.sample_max(),   // max
            rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(
                findmaxConfig.rate_base() + findmaxConfig.rate_step(),
                1.1d,
                SimRateSpec.Verb.restart)))
        );

        SimFrameCapture capture = new SimFrameValueData(flywheel);
        SimFrameFunction frameFunction = new FindmaxFrameFunction(controller, findmaxConfig, flywheel, capture, journal, model);
        FindmaxAnalyzer analyzer = new FindmaxAnalyzer(frameFunction, model);
        FindmaxFrameParams result = analyzer.analyze();
        stdout.println("result:" + result);

        SimFrame<FindmaxFrameParams> best = journal.bestRun();
        stdout.println("bestrun:\n" + best);
        return best.params();
    }

    private class FindmaxAnalyzer {
        private final SimFrameFunction frameFunction;
        private final FindmaxParamModel model;
        public FindmaxAnalyzer(SimFrameFunction frameFunction, FindmaxParamModel model) {
            this.frameFunction = frameFunction;
            this.model = model;
        }

        public FindmaxFrameParams analyze() {
            FindmaxFrameParams frameParams = new FindmaxFrameParams(model, null);

//            while (frameParams != null) {
//                applyParams(frameParams,flywheel);
//                capture.startWindow();
//                capture.awaitSteadyState();
//                capture.stopWindow();
//                journal.record(frameParams, capture.last());
//                stdout.println(capture.last());
//                stdout.println("-".repeat(40));
//                frameParams = nextStep(journal);
//            }
//            return journal.bestRun().params();
            return null;
        }
    }
}

/*
 * Copyright (c) 2020-2024 nosqlbench
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
import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.capture.SimFrameValueData;
import io.nosqlbench.scenarios.simframe.optimizers.CMD_optimize;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunctionAnalyzer;
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
        switch(findmaxConfig.optimization_type()) {
            case "rate" ->
                model.add("rate",
                    findmaxConfig.min_value(),      // min
                    findmaxConfig.base_value(),     // initial
                    findmaxConfig.max_value(),      // max
                    rate -> flywheel.onEvent(ParamChange.of(new CycleRateSpec(
                        rate,
                        1.1d,
                        SimRateSpec.Verb.restart)))
                );
            case "threads" ->
                model.add("threads",
                    findmaxConfig.min_value(),      // min
                    findmaxConfig.base_value(),     // initial
                    findmaxConfig.max_value(),      // max
                    threads -> flywheel.onEvent(ParamChange.of(new SetThreads((int) (threads))))
                );
            default ->
                throw new RuntimeException("Unsupported optimization type: " + findmaxConfig.optimization_type());
        }

        SimFrameCapture capture = new SimFrameValueData(flywheel);
        FindmaxFrameFunction frameFunction = new FindmaxFrameFunction(controller, findmaxConfig, flywheel, capture, journal, model);
        SimFrameFunctionAnalyzer<FindmaxFrameFunction,FindmaxConfig> analyzer = new FindmaxAnalyzer(frameFunction, findmaxConfig);
        SimFrame<? extends InvokableResult> best = analyzer.analyze();
        stdout.println("Best Run:\n" + best);
        return best.params();
    }


}

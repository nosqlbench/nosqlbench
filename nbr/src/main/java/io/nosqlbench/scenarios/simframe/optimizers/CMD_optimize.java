/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.scenarios.simframe.optimizers;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameValueData;
import io.nosqlbench.scenarios.simframe.optimizers.planners.OptimizerPlannerTypes;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;

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
@Service(value = NBBaseCommand.class,selector = "optimize")
public class CMD_optimize extends NBBaseCommand {
    private final static Logger logger = LogManager.getLogger(CMD_optimize.class);

    public CMD_optimize(NBBufferedContainer parentComponent, String scenarioName, String context) {
        super(parentComponent, scenarioName, context);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Activity flywheel = SimFrameUtils.findFlywheelActivity(controller, params.get("activity"));
        stdout.println("starting analysis on activity '" + flywheel.getAlias() + "'");
        SimFrameUtils.awaitActivity(flywheel);
        SimFrameCapture capture = new SimFrameValueData(flywheel);
        String plannerType = params.getOrDefault("planner", "ratchet");
        OptimizerPlannerTypes plannerImpl = OptimizerPlannerTypes.valueOf(plannerType);
        SimFramePlanner<?,?> planner = plannerImpl.createPlanner(this,params);
        Record result = planner.analyze(flywheel, capture, stdout, stderr, controller);
        stdout.println("result:\n" + result);
        return result;
    }

}

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

package io.nosqlbench.scenarios.simframe.planning;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.scenarios.simframe.capture.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;

/**
 * A frame planner is what decides what next set of parameters to try based on a history
 * of simulation frames, and whether to proceed with another sim frame.
 *
 * @param <C>
 *     The configuration type for the planner
 */
public abstract class SimFramePlanner<C,P extends Record> {
    private final Logger logger = LogManager.getLogger(SimFramePlanner.class);
    protected final C config;
    protected final SimFrameJournal<P> journal;

    public SimFramePlanner(NBCommandParams analyzerParams) {
        this.config = getConfig(analyzerParams);
        this.journal = initJournal();
    }

    public abstract C getConfig(NBCommandParams params);

    public SimFrameJournal<P> initJournal() {
        return new SimFrameJournal<>();
    }

    public abstract P initialStep();

    /**
     * Using a stateful history of all control parameters and all results, decide if there
     * is additional search space and return a set of parameters for the next workload
     * simulation frame. If the stopping condition has been met, return null
     *
     * @param journal
     *     All parameters and results, organized in enumerated simulation frames
     * @return Optionally, a set of paramValues which indicates another simulation frame should be sampled, else null
     */
    public abstract P nextStep(JournalView<P> journal);

    public abstract void applyParams(P params, Activity activity);
    public P analyze(Activity flywheel, SimFrameCapture capture, PrintWriter stdout, PrintWriter stderr, ContainerActivitiesController controller) {
        var frameParams = initialStep();

        while (frameParams != null) {
            stdout.println(frameParams);
            applyParams(frameParams,flywheel);
            capture.startWindow();
            capture.awaitSteadyState();
            applyParams(frameParams,flywheel);
            capture.restartWindow();
//            controller.waitMillis(500);
            capture.awaitSteadyState();
            capture.stopWindow();
            journal.record(frameParams, capture.last());
            stdout.println(capture.last());
            stdout.println("-".repeat(40));
            frameParams = nextStep(journal);
        }
        return journal.bestRun().params();

    }



}

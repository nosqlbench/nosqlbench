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
import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;

public class FindmaxFrameFunction implements SimFrameFunction<FindmaxFrameParams> {

    private final Activity flywheel;
    private final SimFrameCapture capture;
    private final SimFrameJournal<FindmaxFrameParams> journal;
    private final FindmaxConfig settings;
    private final ContainerActivitiesController controller;
    private final FindmaxParamModel model;

    public FindmaxFrameFunction(
        ContainerActivitiesController controller,
        FindmaxConfig settings,
        Activity flywheel,
        SimFrameCapture capture,
        SimFrameJournal<FindmaxFrameParams> journal,
        FindmaxParamModel model
    ) {
        this.controller = controller;
        this.settings = settings;
        this.flywheel = flywheel;
        this.capture = capture;
        this.journal = journal;
        this.model = model;
    }

    @Override
    public double value(double[] point) {
        System.out.println("━".repeat(40));
        FindmaxFrameParams params = model.apply(point);
        System.out.println(params);
        capture.startWindow();
        capture.awaitSteadyState();
        model.apply(point);
        capture.restartWindow();
        System.out.println("sampling for " + settings.sample_time_ms()+"ms");
        controller.waitMillis((long) settings.sample_time_ms());
        capture.stopWindow();
        journal.record(params,capture.last());
        System.out.println(journal.last());
        if (flywheel.getRunStateTally().tallyFor(RunState.Running)==0) {
            System.out.println("state:" + flywheel.getRunState());
            throw new RuntimeException("Early exit of flywheel activity '" + flywheel.getAlias() + "'. Can't continue.");
        }
        return journal.last().value();
    }

    @Override
    public SimFrameJournal<FindmaxFrameParams> getJournal() {
        return journal;
    }
}

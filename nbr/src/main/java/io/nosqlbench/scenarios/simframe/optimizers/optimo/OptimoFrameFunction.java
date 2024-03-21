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

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.capture.SimFrameJournal;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunction;

public class OptimoFrameFunction implements SimFrameFunction<OptimoFrameParams> {

    private final Activity flywheel;
    private final SimFrameCapture capture;
    private final SimFrameJournal<OptimoFrameParams> journal;
    private final OptimoSearchSettings settings;
    private final ContainerActivitiesController controller;

    public OptimoFrameFunction(
        ContainerActivitiesController controller,
        OptimoSearchSettings settings,
        Activity flywheel,
        SimFrameCapture capture,
        SimFrameJournal<OptimoFrameParams> journal
    ) {
        this.controller = controller;
        this.settings = settings;
        this.flywheel = flywheel;
        this.capture = capture;
        this.journal = journal;
    }

    @Override
    public double value(double[] point) {
        System.out.println("━".repeat(40));
        OptimoFrameParams params = settings.model().apply(point);
        System.out.println(params);
        capture.startWindow();
        capture.awaitSteadyState();
        settings.model().apply(point);
        capture.restartWindow();
        System.out.println("sampling for " + settings.sample_time_ms()+"ms");
        controller.waitMillis(settings.sample_time_ms());
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
    public SimFrameJournal<OptimoFrameParams> getJournal() {
        return journal;
    }
}

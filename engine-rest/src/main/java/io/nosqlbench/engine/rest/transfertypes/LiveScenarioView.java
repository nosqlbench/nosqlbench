/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.core.script.Scenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LiveScenarioView {

    private final Scenario scenario;

    public LiveScenarioView(Scenario scenario) {
        this.scenario = scenario;
    }

    @JsonProperty
    @JsonPropertyDescription("Optionally populated result, "+
        " present only if there was an error or the scenario is complete")
    public ResultView getResult() {
        return new ResultView(scenario.getResultIfComplete().orElse(null));
    }

    @JsonProperty("scenario_name")
    public String getScenarioName() {
        return scenario.getScenarioName();
    }

    @JsonProperty("started_at")
    public long getStartMillis() {
        return scenario.getStartedAtMillis();
    }

    @JsonProperty("ended_at")
    public long getEndMillis() {
        return scenario.getEndedAtMillis();
    }

    public Scenario.State getState() {
        return scenario.getScenarioState();
    }

    @JsonProperty("progress")
    public List<ProgressView> getProgress() {
        List<ProgressView> progressView = new ArrayList<>();
        if (scenario.getScenarioController()==null) {
            return progressView;
        }

        Collection<? extends ProgressMeterDisplay> meters = scenario.getScenarioController().getProgressMeters();
        for (ProgressMeterDisplay progressMeterDisplay : meters) {
            ProgressView meterView = new ProgressView(progressMeterDisplay);
            progressView.add(meterView);
        }

        return progressView;
    }

}

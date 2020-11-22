package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.core.lifecycle.ScenarioResult;
import io.nosqlbench.engine.core.script.Scenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LiveScenarioView {

    private final Scenario scenario;
    private final ScenarioResult result;

    public LiveScenarioView(Scenario scenario, ScenarioResult result) {
        this.scenario = scenario;
        this.result = result;
    }

    @JsonProperty
    @JsonPropertyDescription("Optionally populated result, "+
        " present only if there was an error or the scenario is complete")
    public ResultView getResult() {
        if (result==null) {
            return null;
        }
        return new ResultView(result);
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

        Collection<? extends ProgressMeter> meters = scenario.getScenarioController().getProgressMeters();
        for (ProgressMeter progressMeter : meters) {
            ProgressView meterView = new ProgressView(progressMeter);
            progressView.add(meterView);
        }

        return progressView;
    }

}

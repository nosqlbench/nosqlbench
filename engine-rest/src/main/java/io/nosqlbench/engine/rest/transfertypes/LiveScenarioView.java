package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.engine.core.ScenarioResult;
import io.nosqlbench.engine.core.script.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LiveScenarioView {

    private final Scenario scenario;
    private final ScenarioResult result;

    public LiveScenarioView(Scenario scenario, ScenarioResult result) {
        this.scenario = scenario;
        this.result = result;
    }

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

    @JsonProperty("progress")
    public List<ProgressView> getProgress() {
        List<ProgressView> progress = new ArrayList<>();

        return scenario.getScenarioController().getProgressMeters()
            .stream().map(ProgressView::new).collect(Collectors.toList());
    }

}

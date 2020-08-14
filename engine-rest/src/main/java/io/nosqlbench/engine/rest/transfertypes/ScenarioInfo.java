package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.engine.core.ActivityExecutor;
import io.nosqlbench.engine.core.ScenarioResult;
import io.nosqlbench.engine.core.script.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScenarioInfo {

    private final Scenario scenario;
    private final ScenarioResult result;

    public ScenarioInfo(Scenario scenario, ScenarioResult result) {
        this.scenario = scenario;
        this.result = result;
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

    @JsonProperty("activity_states")
    public List<Map<String, String>> getActivityStates() {
        List<Map<String, String>> states = new ArrayList<>();
        for (ActivityExecutor ae : scenario.getScenarioController().getActivityExecutorMap().values()) {
            states.add(
                Map.of(
                    "name", ae.getProgressName(),
                    "completion", String.valueOf(ae.getProgress()),
                    "state", ae.getProgressState().toString()
                )
            );
        }
        return states;
    }
}

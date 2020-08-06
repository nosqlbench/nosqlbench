package io.nosqlbench.engine.rest.transfertypes;

import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.core.script.Scenario;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScenarioInfo {
    private final Scenario scenario;

    public ScenarioInfo(Scenario scenario) {
        this.scenario = scenario;
    }

    public String getScenarioName() {
        return scenario.getScenarioName();
    }

    public Map<String,String> getProgress() {
        Map<String,String> progress = new HashMap<>();

        Collection<ProgressMeter> progressMeters =
                scenario.getScenarioController().getProgressMeters();
        for (ProgressMeter meter : progressMeters) {
            String activityName = meter.getProgressName();
            String activityProgress = meter.getProgressDetails();
            if (activityName!=null && activityProgress!=null) {
                progress.put(activityName, activityProgress);
            }
        }
        return progress;
    }
}

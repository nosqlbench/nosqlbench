package io.nosqlbench.engine.rest.transfertypes;

import io.nosqlbench.engine.core.ScenarioResult;

public class ResultInfo {
    private final String scenarioName;
    private final ScenarioResult result;

    public ResultInfo(String scenarioName, ScenarioResult result) {
        this.scenarioName = scenarioName;
        this.result = result;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public boolean isComplete() {
        return result != null;
    }

    public boolean isErrored() {
        return (result != null && result.getException().isPresent());
    }

    public String getIOLog() {
        return result.getIOLog();
    }

}

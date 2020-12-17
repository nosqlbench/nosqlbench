package io.nosqlbench.engine.rest.transfertypes;

import io.nosqlbench.engine.core.lifecycle.ScenarioResult;

public class ResultView {

    private final ScenarioResult result;

    public ResultView(ScenarioResult result) {
        this.result = result;
    }

    public String getIOLog() {
        return result.getIOLog();
    }

    public String getError() {
        if (result.getException().isPresent()) {
            return result.getException().get().getMessage();
        } else {
            return "";
        }
    }
}

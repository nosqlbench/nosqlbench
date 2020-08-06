package io.nosqlbench.engine.rest.transfertypes;

import io.nosqlbench.engine.core.ScenarioResult;

/**
 * TODO: Combine scenario status and pending state to one view
 * <pre>{@code
 *  {
 *      "scenarioName": "myscenarioname",
 *      "isComplete": (true|false),
 *      "isErrored": (true|false),
 *      "ioLog": "IOLOGLine1\n...\n"
 *
 *      [same progress data as for the pending scenario view]
 *
 *      " whole scenario "
 *      [constructed link to grafana dashboard for current duration, with selected update interval]
 *
 *      [create snapshot in grafana from the time range of the scenario once complete]
 *      [link to grafana snapshot]
 *
 *  }
 * }</pre>
 */
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

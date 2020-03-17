package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class RawScenarios extends LinkedHashMap<String, LinkedList<String>> {

    public List<String> getScenarioNames() {
        return new LinkedList<>(this.keySet());
    }

    public List<String> getNamedScenario(String scenarioName) {
        return this.get(scenarioName);
    }
}

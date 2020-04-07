package io.nosqlbench.engine.api.scenarios;

import java.util.List;
import java.util.Set;

public class WorkloadDesc {
    private final String yamlPath;
    private final List<String> scenarioNames;
    private final Set<String> templates;

    public WorkloadDesc(String yamlPath, List<String> scenarioNames, Set<String> templates) {
        this.yamlPath = yamlPath;
        this.scenarioNames = scenarioNames;
        this.templates = templates;
    }

    public String getYamlPath() {
        return yamlPath;
    }

    public String getWorkloadName(){
        return getYamlPath().replaceAll("\\.yaml", "");

    }

    public List<String> getScenarioNames() {
        return scenarioNames;
    }

    public Set<String> getTemplates() {
        return templates;
    }
}

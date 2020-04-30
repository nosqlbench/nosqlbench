package io.nosqlbench.engine.api.scenarios;

import java.util.List;
import java.util.Map;

public class WorkloadDesc {
    private final String yamlPath;
    private final List<String> scenarioNames;
    private final Map<String, String> templates;
    private final String description;

    public WorkloadDesc(String yamlPath,
                        List<String> scenarioNames,
                        Map<String, String> templates,
                        String description) {
        this.yamlPath = yamlPath;
        this.scenarioNames = scenarioNames;
        this.templates = templates;
        this.description = description;
    }

    public String getYamlPath() {
        return yamlPath;
    }

    public String getWorkloadName() {
        return getYamlPath().replaceAll("\\.yaml", "");
    }

    public List<String> getScenarioNames() {
        return scenarioNames;
    }

    public Map<String, String> getTemplates() {
        return templates;
    }

    public String getDescription() {
        return this.description != null ? this.description : "";
    }


    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeScenarios) {

        StringBuilder sb = new StringBuilder();

        if (includeScenarios) {
            sb.append("# workload in ");
        }
        sb.append(getYamlPath()).append("\n");

        if (!description.isEmpty()) {
            sb.append("# description:\n").append(description);
            if (!description.endsWith("\n")) {
                sb.append("\n");
            }
        }
        if (includeScenarios) {
            sb.append("    # scenarios:\n");

            for (String scenario : getScenarioNames()) {
                sb.append("    nb ")
                    .append(this.getWorkloadName())
                    .append(" ").append(scenario).append("\n");
            }


            if (templates.size() > 0) {
                sb.append("        # defaults\n");
            }

            for (Map.Entry<String, String> templateEntry : templates.entrySet()) {
                sb.append("        ")
                    .append(templateEntry.getKey()).append(" = ").append(templateEntry.getValue())
                    .append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();

    }
}

/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.scenarios;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WorkloadDesc implements Comparable<WorkloadDesc> {
    private final String workspace;
    private final String yamlPath;
    private final List<String> scenarioNames;
    private final Map<String, String> templates;
    private final String description;

    public WorkloadDesc(String yamlPath,
                        List<String> scenarioNames,
                        Map<String, String> templates,
                        String description,
                        String workspace) {
        this.yamlPath = yamlPath;
        this.scenarioNames = scenarioNames;
        this.templates = templates;
        this.description = description;
        this.workspace = workspace;
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
        return
            ((workspace != null && !workspace.isEmpty()) ? workspace + ":" : "")
                + this.yamlPath;
//                + (this.description != null ? "\ndesc: " + this.description : "");
    }

    public String toMarkdown(boolean includeScenarios) {

        StringBuilder sb = new StringBuilder();

        if (description.isEmpty()) {
            sb.append("# no description provided\n");
        }

        if (!description.isEmpty()) {
//            sb.append("# description:\n");
            String formattedDesc = "# "+ description.split("[\n.;]")[0];
            sb.append(formattedDesc).append("\n");
            while (sb.toString().endsWith("\n\n")) {
                sb.setLength(sb.length() - 1);
            }
//            if (!description.endsWith("\n")) {
//                sb.append("\n");
//            }
        }

        if (includeScenarios) {
            sb.append("# workload found in ");
        }
        sb.append(getYamlPath()).append("\n");


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

    @Override
    public int compareTo(@NotNull WorkloadDesc o) {
        return this.yamlPath.compareTo(o.yamlPath);
    }

    public WorkloadDesc relativize(Path wsPath) {
        Path yPath = Paths.get(this.yamlPath).toAbsolutePath();
        Path relativePath = wsPath.relativize(yPath);
        String wsName = wsPath.getFileName().toString();

        return new WorkloadDesc(
            relativePath.toString(),
            this.scenarioNames,
            this.templates,
            description,
            wsName
        );
    }

    public String getWorkspace() {
        return workspace;
    }

}

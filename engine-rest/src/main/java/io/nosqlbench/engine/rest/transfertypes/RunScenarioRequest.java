package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class RunScenarioRequest {

    @JsonProperty("commands")
    private List<String> commands = List.of();

    @JsonProperty("filemap")
    private Map<String, String> filemap = Map.of();

    @JsonProperty("console")
    private String stdout;

    @JsonProperty("scenario_name")
    private String scenarioName = "auto";

    @JsonProperty("workspace")
    private String workspace = "default";

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setFileMap(Map<String, String> filemap) {
        this.filemap = filemap;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Map<String, String> getFilemap() {
        return filemap;
    }

    public String getStdout() {
        return stdout;
    }

}

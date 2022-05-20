package io.nosqlbench.engine.rest.transfertypes;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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

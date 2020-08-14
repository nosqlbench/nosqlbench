package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * A CliRequest is what the user sends when they want to invoke NoSQLBench via web request in the
 * same way they may on the command line.
 *
 * <pre>{@code
 *  {
 *    "name" : "auto",
 *    "basedir" : "/tmp/nosqlbench",
 *    "filemap" : {
 *        "file1.yaml": "bindings:\n i: Identity()\n",
 *        "myscb:base64" : "base64encodeddata.."
 *    },
 *    "commands": [
 *      "run", "workload=file1.yaml", "driver=stdout", "cycles=10M", "cyclerate=100", "scb=myscb"
 *    ]
 *  }
 * }</pre>
 */
public class RunScenarioRequest {

    @JsonProperty("commands")
    private List<String> commands;

    @JsonProperty("filemap")
    private Map<String, String> filemap;

    private String stdout;

    @JsonProperty("name")
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

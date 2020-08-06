package io.nosqlbench.engine.rest.transfertypes;

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

    private List<String> commands;
    private Map<String, String> filemap;
    private String stdout;
    private String scenarioName = "auto";
    private String basedir = "/tmp/nosqlbench";

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

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getBasedir() {
        return basedir;
    }
}

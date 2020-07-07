package io.nosqlbench.engine.cli;

import java.util.List;
import java.util.Map;

/**
 * Add cmd
 */
public interface ScriptBuffer {
    /**
     * Add parsed commands to the script buffer
     * @param cmd A parsed command
     * @return This ScriptBuffer
     */
    ScriptBuffer add(Cmd... cmd);

    /**
     * Get the text image of the combined script with
     * all previously added commands included
     * @return The script text
     */
    String getParsedScript();

    /**
     * Get a map which contains all of the params which came from
     * commands of global scope, like {@code script} and {@code fragment} commands.
     * If one of these commands overwrites a named parameter from another,
     * an error should be logged at warning or higher level.
     * @return A globa params map.
     */
    Map<String, String> getCombinedParams();
}

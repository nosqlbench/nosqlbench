package io.nosqlbench.engine.cli;

/**
 * Add cmd
 */
public interface ScriptBuffer {
    ScriptBuffer add(Cmd cmd);
    String getParsedScript();
}

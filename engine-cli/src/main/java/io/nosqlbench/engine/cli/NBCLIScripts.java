package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;

import java.util.List;

public class NBCLIScripts {
    public static void printScripts(boolean includeScenarios,
                                      String... includes) {
        List<String> scripts =
            NBCLIScenarioParser.getScripts(true, includes);

        for (String script: scripts) {
            System.out.println(script);
        }

    }

}

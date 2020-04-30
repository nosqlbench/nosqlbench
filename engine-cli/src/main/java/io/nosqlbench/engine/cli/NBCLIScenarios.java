package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;

import java.util.List;
import java.util.Map;

public class NBCLIScenarios {
    public static void printWorkloads(boolean includeScenarios,
                                      String... includes) {
        List<WorkloadDesc> workloads =
            NBCLIScenarioParser.getWorkloadsWithScenarioScripts(includes);

        for (WorkloadDesc workload : workloads) {
            System.out.println(workload.toString(includeScenarios));


        }

        if (!includeScenarios) {
            System.out.println("\n# To see scenarios scenarios, use --list-scenarios");
        }

        System.out.println(
            "\n" +
                "# To include examples, add --include=examples\n" +
                "# To copy any of these to your local directory, use\n" +
                "# --include=examples --copy=examplename\n"
        );

    }
}

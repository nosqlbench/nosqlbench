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
            if (includeScenarios) {
                System.out.print("# workload in ");
            }

            System.out.println(workload.getYamlPath());

            if (includeScenarios) {
                System.out.println("    # scenarios:");

                List<String> scenarioList = workload.getScenarioNames();
                String workloadName = workload.getWorkloadName();

                for (String scenario : scenarioList) {
                    System.out.println("    nb " + workloadName + " " + scenario);
                }

                Map<String, String> templates = workload.getTemplates();
                if (templates.size() > 0) {
                    System.out.println("        # defaults");
                    for (Map.Entry<String, String> templateEntry : templates.entrySet()) {
                        System.out.println("        " + templateEntry.getKey() + " = " + templateEntry.getValue());
                    }
                }
                System.out.println("\n");
            }

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

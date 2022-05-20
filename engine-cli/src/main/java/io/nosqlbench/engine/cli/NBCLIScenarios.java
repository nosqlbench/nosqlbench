package io.nosqlbench.engine.cli;

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


import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;

import java.util.List;

public class NBCLIScenarios {

    public static void printWorkloads(
        boolean includeScenarios,
        String... includes
    ) {
        List<WorkloadDesc> workloads = List.of();
        try {
            workloads = NBCLIScenarioParser.getWorkloadsWithScenarioScripts(true, includes);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting workloads:" + e.getMessage(), e);

        }
        for (WorkloadDesc workload : workloads) {
            System.out.println(workload.toMarkdown(includeScenarios));
        }

        if (!includeScenarios) {
            System.out.println("## To see scenarios scenarios, use --list-scenarios");
        }

        System.out.println(
            "## To include examples, add --include=examples\n" +
                "## To copy any of these to your local directory, use\n" +
                "## --include=examples --copy=examplename\n"
        );

    }
}

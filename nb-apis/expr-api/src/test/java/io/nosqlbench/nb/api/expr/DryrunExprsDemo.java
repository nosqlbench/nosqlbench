package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
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


import java.net.URI;
import java.util.Map;

/**
 * Demo class to show how dryrun=exprs would display the scripting context.
 * This simulates what OpsLoader.processExpressions does when dryrun=exprs is set.
 */
public class DryrunExprsDemo {

    public static void main(String[] args) {
        ExprPreprocessor preprocessor = new ExprPreprocessor();

        String template = """
            bindings:
              host: localhost
              port: {{basePort = 9042}}
              replica_port: {{= basePort + 1}}

            scenarios:
              default:
                threads: {{threadCount = 10}}
                connections: {{= threadCount * 2}}
                data: {{items = ['apple', 'banana', 'cherry', 'date', 'elderberry']}}

            config:
              longData: {{longData =
                def result = ""
                for (i in 1..15) {
                  result += "Line $i\\n"
                }
                return result
              }}
            """;

        System.out.println("═".repeat(80));
        System.out.println("DEMO: dryrun=exprs Output");
        System.out.println("═".repeat(80));
        System.out.println();

        // Simulate what OpsLoader does with dryrun=exprs
        ProcessingResult result = preprocessor.processWithContext(
            template,
            URI.create("file:///workloads/demo.yaml"),
            Map.of()
        );

        System.out.println("═".repeat(80));
        System.out.println("EXPRESSION-PROCESSED WORKLOAD");
        System.out.println("═".repeat(80));
        System.out.println(result.getOutput());
        System.out.println();

        // Print the scripting context
        System.out.println(result.getFormattedContext());
    }
}

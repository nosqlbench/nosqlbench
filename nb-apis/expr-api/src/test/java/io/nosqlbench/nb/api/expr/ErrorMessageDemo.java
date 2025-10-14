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
 * Demo class to show the formatted error messages.
 * Run this main method to see examples of enhanced error reporting.
 */
public class ErrorMessageDemo {

    public static void main(String[] args) {
        GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

        System.out.println("=".repeat(80));
        System.out.println("DEMO: Enhanced Groovy Expression Error Reporting");
        System.out.println("=".repeat(80));
        System.out.println();

        // Demo 1: Syntax Error
        demoSyntaxError(processor);

        // Demo 2: Undefined Variable
        demoUndefinedVariable(processor);

        // Demo 3: Multiline Expression Error
        demoMultilineError(processor);
    }

    private static void demoSyntaxError(GroovyExpressionProcessor processor) {
        String template = """
            bindings:
              host: localhost
              port: {{= 9042 + }}
              timeout: 5000
            """;

        try {
            processor.process(template, URI.create("file:///workloads/demo_syntax.yaml"), Map.of());
        } catch (ExpressionEvaluationException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void demoUndefinedVariable(GroovyExpressionProcessor processor) {
        String template = """
            scenarios:
              default:
                count: {{threads = 10}}
                result: {{= threads * connectionPool}}
            """;

        try {
            processor.process(template, URI.create("file:///workloads/demo_undefined.yaml"), Map.of());
        } catch (ExpressionEvaluationException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void demoMultilineError(GroovyExpressionProcessor processor) {
        String template = """
            data: {{=
                def values = [10, 20, 30, 40]
                def divisor = 0
                def results = values.collect { it / divisor }
                return results.join(',')
            }}
            """;

        try {
            processor.process(template, URI.create("file:///workloads/demo_multiline.yaml"), Map.of());
        } catch (ExpressionEvaluationException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

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

package io.nosqlbench.nb.api.expr;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test for the expr API module that validates all known types
 * and expressions work together in a single integrated example.
 *
 * <p>This test validates:</p>
 * <ul>
 *   <li>Core utility functions (env, prop, uuid, now, upper, lower, source)</li>
 *   <li>Parameter functions (param, paramOr, hasParam)</li>
 *   <li>Template functions (_templateSet, _templateGet, _templateAlt)</li>
 *   <li>Both template syntaxes (TEMPLATE function, shell-style)</li>
 *   <li>Groovy expressions with shared context</li>
 *   <li>Collections (lists, maps)</li>
 *   <li>Control flow (conditionals, loops)</li>
 *   <li>String interpolation and manipulation</li>
 *   <li>Nested expressions and complex compositions</li>
 *   <li>Functional programming constructs</li>
 *   <li>Type conversions and null handling</li>
 * </ul>
 *
 * <p>This serves as both a functional test and living documentation of the expr system's
 * capabilities.</p>
 */
class ComprehensiveExprFeaturesIT {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();
    private String workload;
    private String processed;

    @BeforeEach
    void loadAndProcessWorkload() throws IOException {
        workload = loadWorkloadResource("workloads/comprehensive_expr_features.yaml");
        assertNotNull(workload, "Workload file should be loaded");

        // Process with test parameters
        Map<String, Object> params = Map.of(
            "mode", "strict",
            "threshold", 42,
            "debug", "true"
        );

        // Apply template rewriting first (this is normally done in OpsLoader)
        String templateRewritten = TemplateRewriter.rewrite(workload);

        // Then process with Groovy expressions
        processed = processor.process(templateRewritten, URI.create("nb://example"), params);
        assertNotNull(processed, "Processed workload should not be null");
    }

    @AfterEach
    void cleanupTemplateState() {
        // Clean up ThreadLocal state from template functions
        io.nosqlbench.nb.api.expr.providers.TemplateExprFunctionsProvider.clearThreadState();
    }

    // === SECTION 1: Core Utility Functions ===

    @Test
    void shouldProcessEnvFunction() {
        assertTrue(processed.contains("env_test:"), "Should contain env_test field");
        assertTrue(processed.contains("env_missing: fallback_value"), "Should use fallback for missing env var");
    }

    @Test
    void shouldProcessPropFunction() {
        assertTrue(processed.contains("prop_test:"), "Should contain prop_test field");
        assertTrue(processed.contains("prop_custom: default_prop_value"), "Should use fallback for missing property");
    }

    @Test
    void shouldGenerateUUID() {
        assertTrue(processed.contains("random_id:"), "Should contain random_id field");
        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        assertTrue(processed.matches("(?s).*random_id: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*"),
            "Should contain valid UUID");
    }

    @Test
    void shouldGenerateTimestamp() {
        assertTrue(processed.contains("timestamp:"), "Should contain timestamp field");
        // ISO-8601 format check (basic pattern)
        assertTrue(processed.matches("(?s).*timestamp: \\d{4}-\\d{2}-\\d{2}T.*Z.*"),
            "Should contain valid ISO-8601 timestamp");
    }

    @Test
    void shouldProcessCaseTransformation() {
        assertTrue(processed.contains("uppercase_example: HELLO WORLD"), "Should uppercase text");
        assertTrue(processed.contains("lowercase_example: hello world"), "Should lowercase text");
    }

    @Test
    void shouldProcessSourceUri() {
        assertTrue(processed.contains("source_uri: nb://example"), "Should contain source URI");
    }

    // === SECTION 2: Parameter Functions ===

    @Test
    void shouldAccessRequiredParameters() {
        assertTrue(processed.contains("required_mode: strict"), "Should access mode parameter");
        assertTrue(processed.contains("required_threshold: 42"), "Should access threshold parameter");
    }

    @Test
    void shouldUseParameterDefaults() {
        assertTrue(processed.contains("optional_timeout: 30"), "Should use default timeout");
        assertTrue(processed.contains("optional_retries: 3"), "Should use default retries");
        assertTrue(processed.contains("optional_name: default-name"), "Should use default name");
    }

    @Test
    void shouldCheckParameterExistence() {
        assertTrue(processed.contains("has_mode: true"), "Should detect existing parameter");
        assertTrue(processed.contains("has_missing: false"), "Should detect missing parameter");
    }

    // === SECTION 3: Template Variable Syntaxes ===

    @Test
    void shouldProcessTemplateFunctionSyntax() {
        assertTrue(processed.contains("template_simple: 1000"), "Should process TEMPLATE with default");
        assertTrue(processed.contains("template_with_dash: 5000"), "Should process TEMPLATE with numeric default");
    }

    @Test
    void shouldProcessTemplateLexicalScoping() {
        // Both should resolve to "initial" due to lexical scoping
        assertTrue(processed.contains("template_set_first: initial"), "Should set shared_value first time");
        assertTrue(processed.contains("template_use_later: initial"), "Should use shared_value second time");
    }

    @Test
    void shouldProcessTemplateConditionalAlternate() {
        assertTrue(processed.contains("template_debug_mode: true"), "Should have debug parameter");
        assertTrue(processed.contains("template_debug_message: verbose_logging_enabled"), "Should use conditional expr for alternate");
    }

    @Test
    void shouldProcessTemplateWithExplicitDefault() {
        assertTrue(processed.contains("template_func_default: 10"), "Should process TEMPLATE with default");
        assertTrue(processed.contains("template_func_no_default: 10"), "Should use lexically scoped value");
    }

    @Test
    void shouldProcessShellVarSyntax() {
        assertTrue(processed.contains("shell_var_simple: localhost"), "Should process shell-style with default");
        assertTrue(processed.contains("shell_var_port: 9042"), "Should process shell-style port");
        assertTrue(processed.contains("shell_var_no_default: localhost"), "Should use lexically scoped value");
    }

    // === SECTION 4: Groovy Expressions ===

    @Test
    void shouldInitializeSharedContext() {
        assertTrue(processed.contains("setup: Context initialized"), "Should initialize context");
    }

    @Test
    void shouldPerformArithmetic() {
        assertTrue(processed.contains("arithmetic_add: 30"), "Should add numbers");
        assertTrue(processed.contains("arithmetic_multiply: 56"), "Should multiply numbers");
        assertTrue(processed.contains("arithmetic_complex: 100"), "Should handle complex arithmetic");
    }

    @Test
    void shouldProcessStringOperations() {
        assertTrue(processed.contains("string_concat: Hello World"), "Should concatenate strings");
        assertTrue(processed.contains("Count is 0, Mode is strict"), "Should interpolate strings");
    }

    // === SECTION 5: Collections ===

    @Test
    void shouldProcessListOperations() {
        assertTrue(processed.contains("add_items: 3"), "Should add items to shared list");
        assertTrue(processed.contains("list_operations: apple, banana, cherry"), "Should join list items");
        assertTrue(processed.contains("list_transformation: 2-4-6-8-10"), "Should transform list");
        assertTrue(processed.contains("list_filtering: 30"), "Should filter and sum list");
    }

    @Test
    void shouldProcessMapOperations() {
        assertTrue(processed.contains("create_map: 3"), "Should create map with 3 entries");
        assertTrue(processed.contains("map_access: localhost:9042"), "Should access map values");
        assertTrue(processed.contains("alice=95"), "Should iterate over map");
        assertTrue(processed.contains("bob=87"), "Should iterate over map");
        assertTrue(processed.contains("charlie=92"), "Should iterate over map");
    }

    // === SECTION 6: Conditionals and Control Flow ===

    @Test
    void shouldProcessTernaryOperator() {
        assertTrue(processed.contains("ternary_example: low"), "Should evaluate ternary (42 <= 50)");
    }

    @Test
    void shouldProcessConditionals() {
        assertTrue(processed.contains("conditional: low"), "Should evaluate if-else (threshold=42)");
    }

    @Test
    void shouldProcessSwitchStatement() {
        assertTrue(processed.contains("switch_example: strict_mode_enabled"), "Should evaluate switch on mode=strict");
    }

    // === SECTION 7: Loops and Iteration ===

    @Test
    void shouldProcessForLoop() {
        assertTrue(processed.contains("loop_sum: 55"), "Should sum 1..10 = 55");
    }

    @Test
    void shouldProcessEachIteration() {
        assertTrue(processed.contains("loop_each: 1, 4, 9, 16, 25"), "Should square each element");
    }

    @Test
    void shouldProcessWhileLoop() {
        assertTrue(processed.contains("loop_while: 15"), "Should sum 1+2+3+4+5 = 15");
    }

    // === SECTION 8: Complex Nested Expressions ===

    @Test
    void shouldProcessNestedFunctions() {
        assertTrue(processed.contains("nested_functions: DEFAULT_SUFFIX"), "Should nest upper and lower functions");
    }

    @Test
    void shouldProcessTemplateWithExpression() {
        assertTrue(processed.contains("template_value_set: 42"), "Should set template value");
        assertTrue(processed.contains("template_in_expr: Value: 42 units"), "Should combine template and expression");
    }

    @Test
    void shouldProcessExpressionResult() {
        assertTrue(processed.contains("expr_in_template_setup: 420"), "Should compute expression (10*42)");
        assertTrue(processed.contains("expr_in_template: 420"), "Should use computed value in template");
    }

    @Test
    void shouldProcessComplexNesting() {
        assertTrue(processed.contains("complex_nested: Result:"), "Should process complex nested expression");
    }

    // === SECTION 9: State Accumulation ===

    @Test
    void shouldAccumulateState() {
        assertTrue(processed.contains("increment1: 1"), "First increment should be 1");
        assertTrue(processed.contains("increment2: 2"), "Second increment should be 2");
        assertTrue(processed.contains("increment3: 3"), "Third increment should be 3");
        assertTrue(processed.contains("final_counter: 3"), "Final counter should be 3");
        assertTrue(processed.contains("final_accumulator: 6"), "Final accumulator should be 1+2+3=6");
    }

    // === SECTION 10: Advanced String Manipulation ===

    @Test
    void shouldProcessRegularExpressions() {
        assertTrue(processed.contains("regex_test: 2025"), "Should extract year with regex");
    }

    @Test
    void shouldProcessStringMethods() {
        assertTrue(processed.contains("string_methods: Hello groovy"), "Should trim, capitalize, and replace");
    }

    @Test
    void shouldProcessSplitAndJoin() {
        assertTrue(processed.contains("split_join: APPLE | BANANA | CHERRY"), "Should split, uppercase, and join");
    }

    // === SECTION 11: Type Conversion ===

    @Test
    void shouldConvertStringToNumber() {
        assertTrue(processed.contains("str_to_int: 200"), "Should convert string to int (123+77)");
        assertTrue(processed.contains("str_to_double: 6.28"), "Should convert string to double (3.14*2)");
    }

    @Test
    void shouldConvertNumberToString() {
        assertTrue(processed.contains("num_to_str: 42 is the answer"), "Should convert number to string");
    }

    @Test
    void shouldProcessBooleanOperations() {
        assertTrue(processed.contains("bool_and: false"), "Should evaluate AND (42<=50 && true = false)");
        assertTrue(processed.contains("bool_or: true"), "Should evaluate OR (false || true = true)");
        assertTrue(processed.contains("bool_not: true"), "Should evaluate NOT (!false = true)");
    }

    // === SECTION 12: Null Handling ===

    @Test
    void shouldHandleSafeNavigation() {
        assertTrue(processed.contains("safe_nav: null_value"), "Should handle null with safe navigation");
    }

    @Test
    void shouldHandleElvisOperator() {
        assertTrue(processed.contains("elvis: default_value"), "Should use Elvis operator for null");
    }

    @Test
    void shouldHandleNullCoalescing() {
        assertTrue(processed.contains("null_coalesce: found"), "Should find first non-null value");
    }

    // === SECTION 13: Functional Programming ===

    @Test
    void shouldProcessMapCollect() {
        assertTrue(processed.contains("map_example: 55"), "Should map/collect squares and sum (1+4+9+16+25)");
    }

    @Test
    void shouldProcessFilterFindAll() {
        assertTrue(processed.contains("filter_example: 63"), "Should filter multiples of 3 and sum (3+6+9+12+15+18)");
    }

    @Test
    void shouldProcessReduce() {
        assertTrue(processed.contains("reduce_example: 15"), "Should reduce/inject sum");
    }

    @Test
    void shouldProcessAnyAll() {
        assertTrue(processed.contains("any_example: true"), "Should check any > 3");
        assertTrue(processed.contains("all_example: true"), "Should check all > 0");
    }

    // === SECTION 14: Date and Time ===

    @Test
    void shouldProcessTimeOperations() {
        assertTrue(processed.contains("time_operations: Now:"), "Should process time operations");
        assertTrue(processed.contains("Future:"), "Should calculate future time");
    }

    // === SECTION 15: Ultimate Complex Expression ===

    @Test
    void shouldProcessUltimateExpression() {
        assertTrue(processed.contains("current_timestamp:"), "Should have timestamp field");
        assertTrue(processed.contains("generated_id:"), "Should have UUID field");
        assertTrue(processed.contains("ultimate_expression:"), "Should contain ultimate expression");
        assertTrue(processed.contains("=== Comprehensive Expr Test Results ==="), "Should contain header");
        assertTrue(processed.contains("Mode: strict"), "Should include mode in output");
        assertTrue(processed.contains("Count:"), "Should include count in output");
        assertTrue(processed.contains("Sum:"), "Should include sum in output");
        assertTrue(processed.contains("Average:"), "Should include average in output");
        assertTrue(processed.contains("Batch Size: 1000"), "Should include batch size");
        assertTrue(processed.contains("Timeout: 30"), "Should include timeout");
    }

    // === INTEGRATION TESTS ===

    @Test
    void shouldProcessEntireWorkloadWithoutErrors() {
        assertNotNull(processed, "Processed workload should not be null");
        assertFalse(processed.isEmpty(), "Processed workload should not be empty");
        assertFalse(processed.contains("ERROR"), "Processed workload should not contain errors");
        assertFalse(processed.contains("Exception"), "Processed workload should not contain exceptions");
    }

    @Test
    void shouldContainAllMajorSections() {
        String[] sections = {
            "SECTION 1: Core Utility Functions",
            "SECTION 2: Parameter Functions",
            "SECTION 3: Template Variable Syntaxes",
            "SECTION 4: Groovy Expressions",
            "SECTION 5: Collections",
            "SECTION 6: Conditionals and Control Flow",
            "SECTION 7: Loops and Iteration",
            "SECTION 8: Complex Nested Expressions",
            "SECTION 9: State Accumulation",
            "SECTION 10: Advanced String Manipulation",
            "SECTION 11: Type Conversion",
            "SECTION 12: Null Handling",
            "SECTION 13: Functional Programming",
            "SECTION 14: Date and Time",
            "SECTION 15: Combining Everything"
        };

        for (String section : sections) {
            assertTrue(workload.contains(section),
                "Workload should contain section: " + section);
        }
    }

    @Test
    void shouldDemonstrateAllExprFunctionProviders() {
        // CoreExprFunctionsProvider
        assertTrue(processed.contains("env_test:"), "Should use env() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("prop_test:"), "Should use prop() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("random_id:"), "Should use uuid() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("timestamp:"), "Should use now() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("uppercase_example:"), "Should use upper() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("lowercase_example:"), "Should use lower() from CoreExprFunctionsProvider");
        assertTrue(processed.contains("source_uri:"), "Should use source() from CoreExprFunctionsProvider");

        // ParameterExprFunctionsProvider
        assertTrue(processed.contains("required_mode:"), "Should use param() from ParameterExprFunctionsProvider");
        assertTrue(processed.contains("optional_timeout:"), "Should use paramOr() from ParameterExprFunctionsProvider");
        assertTrue(processed.contains("has_mode:"), "Should use hasParam() from ParameterExprFunctionsProvider");

        // TemplateExprFunctionsProvider (via TemplateRewriter)
        assertTrue(processed.contains("template_set_first:"), "Should use _templateSet() from TemplateExprFunctionsProvider");
        assertTrue(processed.contains("template_use_later:"), "Should use _templateGet() from TemplateExprFunctionsProvider");
        assertTrue(processed.contains("template_debug_message:"), "Should use conditional expr for alternate behavior");
    }

    @Test
    void shouldDemonstrateAllTemplateSyntaxes() {
        // TEMPLATE function syntax
        assertTrue(workload.contains("TEMPLATE(worker_count,10)"), "Should contain TEMPLATE function syntax");
        assertTrue(processed.contains("template_func_default: 10"), "Should process TEMPLATE function syntax");

        // Shell-style syntax
        assertTrue(workload.contains("${target_host:localhost}"), "Should contain shell-style syntax");
        assertTrue(processed.contains("shell_var_simple: localhost"), "Should process shell-style syntax");
    }

    @Test
    void shouldDemonstrateTemplateLexicalScoping() {
        // TEMPLATE function provides lexical scoping
        assertTrue(workload.contains("TEMPLATE(shared_value,initial)"), "Should contain TEMPLATE with default");
        assertTrue(workload.contains("TEMPLATE(shared_value)"), "Should contain TEMPLATE without default (reuses value)");

        // Both should resolve to "initial" due to lexical scoping
        assertTrue(processed.contains("template_set_first: initial"), "Should set shared_value first time");
        assertTrue(processed.contains("template_use_later: initial"), "Should reuse shared_value second time");

        // Note: Required parameter syntax would error if parameter not provided
        assertTrue(workload.contains("Required parameter syntax"), "Should document required parameter behavior");
    }

    @Test
    void shouldMaintainConsistentStateThroughoutWorkload() {
        // Verify that shared context is maintained throughout
        assertTrue(processed.contains("setup: Context initialized"), "Should initialize context");
        assertTrue(processed.contains("final_counter: 3"), "Should maintain counter through multiple increments");
        assertTrue(processed.contains("final_accumulator: 6"), "Should maintain accumulator through multiple operations");
    }

    private String loadWorkloadResource(String resourcePath) throws IOException {
        Path path = Path.of("src/test/resources", resourcePath);
        assertTrue(Files.exists(path), "Resource file should exist: " + path);
        return Files.readString(path);
    }
}

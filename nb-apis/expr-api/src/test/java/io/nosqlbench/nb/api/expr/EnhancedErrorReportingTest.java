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


import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying enhanced error reporting for Groovy expression failures.
 * These tests ensure that error messages provide:
 * - The location in the workload file where the error occurred
 * - The line within the Groovy script that caused the error
 * - Visual indicators pointing to the error location
 */
class EnhancedErrorReportingTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldReportSyntaxErrorWithLocation() {
        String template = """
            line1: Some text
            line2: {{= 1 + }}
            line3: More text
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://syntax_error.yaml"), Map.of())
        );

        String message = ex.getMessage();

        // Should contain error type and description
        assertTrue(message.contains("ERROR EVALUATING GROOVY EXPRESSION"), "Should have header");

        // Should contain workload location
        assertTrue(message.contains("WORKLOAD LOCATION"), "Should have workload location section");
        assertTrue(message.contains("test://syntax_error.yaml"), "Should show file URI");
        assertTrue(message.contains("Line: 2"), "Should show line number");

        // Should contain the template line
        assertTrue(message.contains("line2: {{= 1 + }}"), "Should show template line");

        // Should contain groovy expression section
        assertTrue(message.contains("GROOVY EXPRESSION"), "Should have groovy expression section");
    }

    @Test
    void shouldReportUndefinedVariableError() {
        String template = """
            setup: {{myVar = 10}}
            error: {{= undefinedVariable + 5}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://undefined_var.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("Line: 2"), "Should identify line 2");
        assertTrue(message.contains("undefinedVariable"), "Should mention the undefined variable");
    }

    @Test
    void shouldReportErrorInMultilineExpression() {
        String template = """
            data: {{=
                def x = 10
                def y = 0
                return x / y
            }}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://multiline_error.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("GROOVY EXPRESSION"), "Should have groovy section");
        assertTrue(message.contains("multiline"), "Should indicate multiline expression");
        assertTrue(message.contains("def x = 10"), "Should show first line");
        assertTrue(message.contains("def y = 0"), "Should show second line");
        assertTrue(message.contains("return x / y"), "Should show error line");
    }

    @Test
    void shouldReportMethodNotFoundError() {
        String template = """
            result: {{= "hello".nonExistentMethod()}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://method_error.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("nonExistentMethod"), "Should mention the missing method");
        assertTrue(message.contains("Line: 1"), "Should identify line 1");
    }

    @Test
    void shouldReportTypeErrorWithContext() {
        String template = """
            numbers: {{items = [1, 2, 3]}}
            bad: {{= items.toUpperCase()}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://type_error.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("Line: 2"), "Should identify line 2");
        assertTrue(message.contains("toUpperCase"), "Should mention the incorrect method");
    }

    @Test
    void shouldReportNullPointerError() {
        String template = """
            setup: {{value = null}}
            error: {{= value.length()}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://npe.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("Line: 2"), "Should identify line 2");
    }

    @Test
    void shouldShowVisualPointerForSingleLineExpression() {
        String template = "value: {{= badVariable}}";

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://pointer.yaml"), Map.of())
        );

        String message = ex.getMessage();

        // Should contain visual pointer under the expression
        assertTrue(message.contains("^"), "Should have pointer indicator");
    }

    @Test
    void shouldHandleErrorInNestedExpressionContext() {
        String template = """
            outer: {{=
                inner = [1, 2, 3]
                result = inner.collect { it / 0 }
                return result
            }}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://nested.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("GROOVY EXPRESSION"), "Should have groovy section");
        // The expression should be shown
        assertTrue(message.contains("inner = [1, 2, 3]") ||
                   message.contains("result = inner.collect"),
                   "Should show the expression content");
    }

    @Test
    void shouldProvideContextForClassNotFoundError() {
        String template = """
            result: {{= new NonExistentClass()}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://class_not_found.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("NonExistentClass"), "Should mention the missing class");
    }

    @Test
    void shouldTrackMultipleExpressionsCorrectly() {
        String template = """
            first: {{= 1 + 1}}
            second: {{= 2 + 2}}
            third: {{= unknownVar}}
            fourth: {{= 4 + 4}}
            """;

        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://multiple.yaml"), Map.of())
        );

        String message = ex.getMessage();

        // Should correctly identify the third line as the error location
        assertTrue(message.contains("Line: 3"), "Should identify line 3");
        assertTrue(message.contains("unknownVar"), "Should show the error expression");
    }

    @Test
    void shouldProvideContextForAssignmentOperatorErrors() {
        String template = """
            first: {{myVar === 10}}
            second: {{myVar === 20}}
            """;

        // This should fail because === requires variable to not exist
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> processor.process(template, URI.create("test://assignment.yaml"), Map.of())
        );

        String message = ex.getMessage();
        assertTrue(message.contains("myVar") && message.contains("already set"),
                   "Should explain the assignment error");
    }

    @Test
    void shouldHandleParseErrorsInComplexExpressions() {
        String template = """
            data: {{=
                def list = [
                    1, 2, 3,
                    4, 5
                return list.sum()
            }}
            """;

        // Missing closing bracket
        ExpressionEvaluationException ex = assertThrows(
            ExpressionEvaluationException.class,
            () -> processor.process(template, URI.create("test://parse_error.yaml"), Map.of())
        );

        String message = ex.getMessage();

        assertTrue(message.contains("GROOVY EXPRESSION"), "Should have groovy section");
        assertTrue(message.contains("ERROR EVALUATING"), "Should have error header");
    }
}

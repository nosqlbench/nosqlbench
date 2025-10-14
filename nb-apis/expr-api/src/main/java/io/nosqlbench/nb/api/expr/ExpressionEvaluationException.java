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


import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.Optional;

/**
 * Exception thrown when a Groovy expression fails to evaluate, providing detailed
 * context about the error location in both the workload template and the Groovy script.
 */
public class ExpressionEvaluationException extends RuntimeException {

    private final ExpressionContext context;
    private final Optional<Integer> groovyLineNumber;
    private final Optional<Integer> groovyColumnNumber;

    public ExpressionEvaluationException(
        String message,
        Throwable cause,
        ExpressionContext context,
        Optional<Integer> groovyLineNumber,
        Optional<Integer> groovyColumnNumber
    ) {
        super(formatDetailedMessage(message, cause, context, groovyLineNumber, groovyColumnNumber), cause);
        this.context = context;
        this.groovyLineNumber = groovyLineNumber;
        this.groovyColumnNumber = groovyColumnNumber;
    }

    public ExpressionContext getContext() {
        return context;
    }

    public Optional<Integer> getGroovyLineNumber() {
        return groovyLineNumber;
    }

    public Optional<Integer> getGroovyColumnNumber() {
        return groovyColumnNumber;
    }

    /**
     * Extract error location information from Groovy exceptions.
     */
    public static ExpressionLocationInfo extractLocationInfo(Throwable cause) {
        // Try to extract from MultipleCompilationErrorsException
        if (cause instanceof MultipleCompilationErrorsException multiError) {
            var errorCollector = multiError.getErrorCollector();
            if (errorCollector.getErrorCount() > 0) {
                var firstError = errorCollector.getError(0);
                if (firstError instanceof SyntaxErrorMessage syntaxError) {
                    SyntaxException syntaxException = syntaxError.getCause();
                    return new ExpressionLocationInfo(
                        Optional.of(syntaxException.getLine()),
                        Optional.of(syntaxException.getStartColumn())
                    );
                }
            }
        }

        // Try to extract from SyntaxException
        if (cause instanceof SyntaxException syntaxEx) {
            return new ExpressionLocationInfo(
                Optional.of(syntaxEx.getLine()),
                Optional.of(syntaxEx.getStartColumn())
            );
        }

        // Check if the cause's cause has location info
        if (cause.getCause() != null) {
            return extractLocationInfo(cause.getCause());
        }

        // No location info available
        return new ExpressionLocationInfo(Optional.empty(), Optional.empty());
    }

    private static String formatDetailedMessage(
        String baseMessage,
        Throwable cause,
        ExpressionContext context,
        Optional<Integer> groovyLineNumber,
        Optional<Integer> groovyColumnNumber
    ) {
        StringBuilder message = new StringBuilder();
        message.append("\n");
        message.append("═".repeat(80)).append("\n");
        message.append("ERROR EVALUATING GROOVY EXPRESSION\n");
        message.append("═".repeat(80)).append("\n\n");

        // Error type and basic message
        message.append("Error Type: ").append(cause.getClass().getSimpleName()).append("\n");
        message.append("Message: ").append(extractErrorMessage(cause)).append("\n\n");

        // Workload location information
        message.append("─".repeat(80)).append("\n");
        message.append("WORKLOAD LOCATION\n");
        message.append("─".repeat(80)).append("\n");
        message.append("File: ").append(context.getSourceDescription()).append("\n");
        message.append("Line: ").append(context.getTemplateLineNumber()).append("\n");
        message.append("Column: ").append(context.getTemplateColumnStart()).append("\n\n");

        // Show the template line with visual indicator
        message.append("Template Line:\n");
        message.append("  ").append(context.getTemplateLine()).append("\n");
        message.append("  ").append(createPointer(context.getTemplateColumnStart(), context.getTemplateColumnEnd())).append("\n\n");

        // Groovy expression details
        message.append("─".repeat(80)).append("\n");
        message.append("GROOVY EXPRESSION\n");
        message.append("─".repeat(80)).append("\n");

        String[] expressionLines = context.getExpressionText().split("\n");
        if (expressionLines.length == 1) {
            message.append("Expression: ").append(context.getExpressionText()).append("\n");
        } else {
            message.append("Expression (multiline):\n");
            for (int i = 0; i < expressionLines.length; i++) {
                int lineNum = i + 1;
                String linePrefix = String.format("  %2d | ", lineNum);
                message.append(linePrefix).append(expressionLines[i]).append("\n");

                // Add pointer if this is the error line
                if (groovyLineNumber.isPresent() && groovyLineNumber.get() == lineNum) {
                    int pointerColumn = groovyColumnNumber.orElse(1);
                    message.append(" ".repeat(linePrefix.length()))
                        .append(" ".repeat(Math.max(0, pointerColumn - 1)))
                        .append("^ Error here\n");
                }
            }
        }

        if (groovyLineNumber.isPresent()) {
            message.append("\nError at line ").append(groovyLineNumber.get());
            if (groovyColumnNumber.isPresent()) {
                message.append(", column ").append(groovyColumnNumber.get());
            }
            message.append(" in the Groovy script\n");
        }

        message.append("\n");
        message.append("═".repeat(80)).append("\n");

        return message.toString();
    }

    private static String extractErrorMessage(Throwable cause) {
        String msg = cause.getMessage();
        if (msg != null && !msg.isEmpty()) {
            // Clean up common Groovy error message prefixes
            msg = msg.replaceFirst("^startup failed:\\s*", "");
            msg = msg.replaceFirst("^Script\\d+\\.groovy: \\d+: ", "");
            return msg;
        }
        return cause.getClass().getSimpleName();
    }

    private static String createPointer(int startCol, int endCol) {
        int adjustedStart = Math.max(0, startCol);
        int adjustedEnd = Math.max(adjustedStart + 1, endCol);
        int pointerLength = adjustedEnd - adjustedStart;

        StringBuilder pointer = new StringBuilder();
        pointer.append(" ".repeat(adjustedStart));
        if (pointerLength == 1) {
            pointer.append("^");
        } else {
            pointer.append("^");
            pointer.append("~".repeat(Math.max(0, pointerLength - 2)));
            if (pointerLength > 1) {
                pointer.append("^");
            }
        }
        return pointer.toString();
    }

    /**
     * Holds location information extracted from Groovy exceptions.
     */
    public static class ExpressionLocationInfo {
        private final Optional<Integer> lineNumber;
        private final Optional<Integer> columnNumber;

        public ExpressionLocationInfo(Optional<Integer> lineNumber, Optional<Integer> columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public Optional<Integer> getLineNumber() {
            return lineNumber;
        }

        public Optional<Integer> getColumnNumber() {
            return columnNumber;
        }
    }
}

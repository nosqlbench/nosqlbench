/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.mql.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom error listener for MetricsQL parser that provides user-friendly error messages.
 *
 * <p>Instead of raw ANTLR error messages, this listener translates them into
 * actionable feedback for users writing MetricsQL queries.</p>
 *
 * <p>Example error transformations:</p>
 * <ul>
 *   <li>"mismatched input" → explains what was expected in MetricsQL syntax</li>
 *   <li>"extraneous input" → suggests valid syntax</li>
 *   <li>"no viable alternative" → provides query syntax hints</li>
 * </ul>
 */
public class MetricsQLErrorListener extends BaseErrorListener {
    private static final Logger logger = LoggerFactory.getLogger(MetricsQLErrorListener.class);

    private final List<String> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                           Object offendingSymbol,
                           int line,
                           int charPositionInLine,
                           String msg,
                           RecognitionException e) {
        String errorMessage = buildUserFriendlyError(line, charPositionInLine, msg, offendingSymbol);
        errors.add(errorMessage);
        logger.debug("Parse error at {}:{} - {}", line, charPositionInLine, msg);
    }

    /**
     * Builds a user-friendly error message from ANTLR's raw error.
     */
    private String buildUserFriendlyError(int line, int charPositionInLine, String msg, Object offendingSymbol) {
        StringBuilder error = new StringBuilder();
        error.append("MetricsQL syntax error at line ").append(line)
             .append(", position ").append(charPositionInLine).append(":\n");

        // Translate common ANTLR messages to user-friendly explanations
        if (msg.contains("mismatched input")) {
            error.append("  Unexpected token. ");
            if (msg.contains("expecting")) {
                String expecting = extractExpected(msg);
                error.append("Expected: ").append(expecting).append("\n");
            }
            error.append("  Check your MetricsQL syntax. Common patterns:\n");
            error.append("    - metric_name\n");
            error.append("    - metric_name{label=\"value\"}\n");
            error.append("    - metric_name{label=\"value\"}[5m]\n");
            error.append("    - rate(metric[5m])\n");
            error.append("    - sum(metric) by (label)\n");

        } else if (msg.contains("extraneous input")) {
            error.append("  Unexpected extra input. ");
            error.append("This token doesn't fit here.\n");
            error.append("  Hint: Check for missing operators, commas, or parentheses.\n");

        } else if (msg.contains("token recognition error")) {
            error.append("  Invalid character or string. ");
            error.append("Token: ").append(offendingSymbol).append("\n");
            error.append("  Hint: Metric names must start with letter/underscore/colon.\n");
            error.append("        Label values must be enclosed in double quotes.\n");

        } else if (msg.contains("no viable alternative")) {
            error.append("  Cannot parse this query structure.\n");
            error.append("  Hint: Check that your query follows MetricsQL syntax:\n");
            error.append("    - Functions: rate(metric[5m]), increase(metric[5m])\n");
            error.append("    - Aggregations: sum(metric) by (label)\n");
            error.append("    - Operators: metric1 + metric2, metric > 100\n");

        } else if (msg.contains("missing")) {
            String missing = extractMissing(msg);
            error.append("  Missing required token: ").append(missing).append("\n");
            error.append("  Hint: Check for unclosed brackets, parentheses, or quotes.\n");

        } else {
            // Default: include the raw message
            error.append("  ").append(msg).append("\n");
        }

        return error.toString();
    }

    /**
     * Extracts the expected tokens from ANTLR's error message.
     */
    private String extractExpected(String msg) {
        int expectingIndex = msg.indexOf("expecting");
        if (expectingIndex != -1) {
            return msg.substring(expectingIndex + "expecting".length()).trim();
        }
        return "valid MetricsQL syntax";
    }

    /**
     * Extracts the missing token from ANTLR's error message.
     */
    private String extractMissing(String msg) {
        int missingIndex = msg.indexOf("missing");
        if (missingIndex != -1) {
            int atIndex = msg.indexOf(" at ", missingIndex);
            if (atIndex != -1) {
                return msg.substring(missingIndex + "missing".length(), atIndex).trim();
            }
        }
        return "token";
    }

    /**
     * Returns all accumulated errors.
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns true if any errors were encountered.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns all errors as a single formatted string.
     */
    public String getErrorsAsString() {
        return String.join("\n", errors);
    }

    /**
     * Clears all accumulated errors.
     */
    public void clearErrors() {
        errors.clear();
    }
}

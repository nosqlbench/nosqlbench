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


import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Formats binding context variables for display in a compact format.
 * Uses single-line format for short values and multi-line format for longer values.
 */
public class ContextFormatter {

    private static final int MAX_LINES_BEFORE_ABBREVIATION = 10;
    private static final int SINGLE_LINE_MAX_LENGTH = 80;
    private static final String INTERNAL_VAR_PREFIX = "_";
    private static final String EXPR_LVAR_PREFIX = "__expr_lvar_";

    /**
     * Format all variables in the binding context, excluding internal variables.
     * Only shows variables that were added by user scripts (not initial framework variables).
     *
     * @param binding the Groovy binding containing all variables
     * @param initialVariables variables present before user scripts ran (to be excluded)
     * @return formatted string representation of the context
     */
    public static String formatContext(groovy.lang.Binding binding, Set<String> initialVariables) {
        if (binding == null) {
            return "";
        }

        Map<String, Object> variables = binding.getVariables();
        if (variables.isEmpty()) {
            return "\nSCRIPTING CONTEXT (empty)\n";
        }

        StringBuilder output = new StringBuilder();
        output.append("\n");
        output.append("SCRIPTING CONTEXT:\n");

        // Sort variables for consistent output
        TreeMap<String, Object> sortedVars = new TreeMap<>(variables);

        boolean hasVisibleVars = false;
        for (Map.Entry<String, Object> entry : sortedVars.entrySet()) {
            String name = entry.getKey();

            // Skip internal variables and initial framework variables
            if (shouldSkipVariable(name) || initialVariables.contains(name)) {
                continue;
            }

            hasVisibleVars = true;
            Object value = entry.getValue();

            output.append(formatVariable(name, value));
        }

        if (!hasVisibleVars) {
            return "\nSCRIPTING CONTEXT: (no user variables)\n";
        }

        return output.toString();
    }

    /**
     * Format all variables in the binding context, excluding internal variables.
     * Shows all user variables (legacy method for backward compatibility).
     *
     * @param binding the Groovy binding containing all variables
     * @return formatted string representation of the context
     */
    public static String formatContext(groovy.lang.Binding binding) {
        return formatContext(binding, Set.of());
    }

    /**
     * Determine if a variable should be skipped from output.
     */
    private static boolean shouldSkipVariable(String name) {
        // Skip internal variables starting with underscore
        if (name.startsWith(INTERNAL_VAR_PREFIX)) {
            return true;
        }
        // Skip the prefixed lvar versions (we show the unprefixed ones)
        if (name.startsWith(EXPR_LVAR_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * Format a value to string representation.
     * If the type has no direct toString method, use indirect description.
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }

        // Check if the class has its own toString implementation (not from Object)
        if (hasCustomToString(value)) {
            return value.toString();
        } else {
            // Use indirect description with class name in brackets
            return "[" + value.getClass().getName() + "]";
        }
    }

    /**
     * Check if a class has overridden toString() from Object.
     */
    private static boolean hasCustomToString(Object value) {
        try {
            Method toStringMethod = value.getClass().getMethod("toString");
            // If toString is declared in Object, it's not custom
            return toStringMethod.getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Abbreviate output if it has more than MAX_LINES_BEFORE_ABBREVIATION lines.
     * Shows first line, a summary indicator, and last line.
     */
    private static String abbreviateIfNeeded(String text) {
        if (text == null || text.isEmpty()) {
            return "(empty)";
        }

        // Split the text by newlines
        String[] lines = text.split("\n", -1);

        if (lines.length <= MAX_LINES_BEFORE_ABBREVIATION) {
            return text;
        }

        // Abbreviate: show first line, indicator, and last line
        StringBuilder abbreviated = new StringBuilder();
        abbreviated.append(lines[0]).append("\n");

        int hiddenLines = lines.length - 2; // -2 for first and last
        abbreviated.append(String.format("  [... (%d lines) ...]\n", hiddenLines));

        if (lines.length > 1) {
            abbreviated.append(lines[lines.length - 1]);
        }

        return abbreviated.toString();
    }

    /**
     * Format a single variable for display in compact format.
     * Uses single-line format for short values, multi-line for longer values.
     *
     * @param name the variable name
     * @param value the variable value
     * @return formatted string representation
     */
    public static String formatVariable(String name, Object value) {
        String valueStr = formatValue(value);
        String abbreviated = abbreviateIfNeeded(valueStr);

        // Check if we can use single-line format
        if (!abbreviated.contains("\n") && abbreviated.length() < SINGLE_LINE_MAX_LENGTH) {
            // Single-line format: "varname: value"
            return name + ": " + abbreviated + "\n";
        } else {
            // Multi-line format with 2-space indent
            StringBuilder output = new StringBuilder();
            output.append(name).append(":\n");

            String[] lines = abbreviated.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (i > 0 || !lines[i].isEmpty()) {
                    output.append("  ").append(lines[i]);
                    if (i < lines.length - 1) {
                        output.append("\n");
                    }
                }
            }
            output.append("\n");

            return output.toString();
        }
    }
}

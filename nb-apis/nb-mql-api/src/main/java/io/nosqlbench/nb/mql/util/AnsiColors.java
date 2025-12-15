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

package io.nosqlbench.nb.mql.util;

/**
 * ANSI color codes for terminal output colorization.
 */
public class AnsiColors {
    // Reset
    public static final String RESET = "\u001B[0m";

    // Regular colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright colors
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Bold
    public static final String BOLD = "\u001B[1m";

    // Semantic colors for tree elements
    public static final String TREE_BRANCH = BRIGHT_BLACK;      // Tree structure characters
    public static final String LABEL_KEY = CYAN;                 // Label keys (endpoint, method, etc.)
    public static final String LABEL_VALUE = BRIGHT_CYAN;        // Label values (=/users, =GET, etc.)
    public static final String METRIC_NAME = GREEN;              // Metric names (api_latency, etc.)
    public static final String COMMON_LABEL = YELLOW;            // Common labels header

    private static boolean colorsEnabled = true;

    /**
     * Enable or disable color output.
     */
    public static void setColorsEnabled(boolean enabled) {
        colorsEnabled = enabled;
    }

    /**
     * Check if colors are enabled.
     */
    public static boolean isColorsEnabled() {
        return colorsEnabled;
    }

    /**
     * Colorize a string with the given color code.
     * If colors are disabled, returns the string unchanged.
     */
    public static String colorize(String text, String colorCode) {
        if (!colorsEnabled || text == null) {
            return text;
        }
        return colorCode + text + RESET;
    }

    /**
     * Colorize tree branch characters (├──, └──, │).
     */
    public static String colorizeTreeBranch(String text) {
        return colorize(text, TREE_BRANCH);
    }

    /**
     * Colorize a label (key=value pair).
     * Applies different colors to the key and value parts.
     * Handles comma-separated labels (e.g., "method=GET,method=POST").
     */
    public static String colorizeLabel(String label) {
        if (!colorsEnabled || label == null) {
            return label;
        }

        // Check if this is a comma-separated list of labels
        if (label.contains(",")) {
            String[] parts = label.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append(",");
                }
                result.append(colorizeSingleLabel(parts[i]));
            }
            return result.toString();
        }

        return colorizeSingleLabel(label);
    }

    /**
     * Colorize a single label (key=value pair).
     */
    private static String colorizeSingleLabel(String label) {
        // Split on first '=' to separate key from value
        int eqIndex = label.indexOf('=');
        if (eqIndex == -1) {
            return colorize(label, LABEL_KEY);
        }

        String key = label.substring(0, eqIndex);
        String value = label.substring(eqIndex);

        return colorize(key, LABEL_KEY) + colorize(value, LABEL_VALUE);
    }

    /**
     * Colorize a metric name.
     */
    public static String colorizeMetric(String metric) {
        return colorize(metric, METRIC_NAME);
    }

    /**
     * Colorize common labels header.
     */
    public static String colorizeCommonLabel(String text) {
        return colorize(text, COMMON_LABEL);
    }
}

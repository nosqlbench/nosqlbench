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

package io.nosqlbench.nb.mql.format;

import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.util.AnsiColors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom markdown formatter for metrics command output.
 * Creates a tree-structured report with metrics statistics as leaf nodes.
 */
public class MetricsMarkdownFormatter implements ResultFormatter {

    @Override
    public String format(QueryResult result) {
        if (result.isEmpty()) {
            return "*No metrics available.*\n";
        }

        StringBuilder md = new StringBuilder();

        // Title
        md.append("# NoSQLBench Metrics Inventory\n\n");

        // Group rows by section
        Map<String, List<Map<String, Object>>> sections = new LinkedHashMap<>();
        for (Map<String, Object> row : result.rows()) {
            String section = (String) row.get("section");
            if (section == null || section.isEmpty()) {
                continue; // Skip blank separator rows
            }
            sections.computeIfAbsent(section, k -> new ArrayList<>()).add(row);
        }

        // Render each section
        for (Map.Entry<String, List<Map<String, Object>>> entry : sections.entrySet()) {
            String sectionName = entry.getKey();
            List<Map<String, Object>> sectionRows = entry.getValue();

            if ("SESSION".equals(sectionName)) {
                renderSessionSection(md, sectionRows);
            } else if ("COMMON LABELS".equals(sectionName)) {
                renderCommonLabelsSection(md, sectionRows);
            } else if ("METRICS".equals(sectionName)) {
                // Collect metrics data for tree rendering
                continue; // Will be rendered with tree
            } else if ("LABEL SETS".equals(sectionName)) {
                // Get metrics data for adding as leaf nodes
                List<Map<String, Object>> metricsRows = sections.getOrDefault("METRICS", new ArrayList<>());
                renderLabelSetTree(md, sectionRows, metricsRows);
            }
        }

        // Footer
        md.append("---\n\n");
        md.append(String.format("*Report generated in %dms*\n", result.executionTimeMs()));

        return md.toString();
    }

    private void renderSessionSection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 📊 Session Information\n\n");

        for (Map<String, Object> row : rows) {
            String item = (String) row.get("item");
            String value = (String) row.get("value");

            if (item != null && !item.isEmpty()) {
                md.append("**").append(item).append(":**  \n");
                md.append("`").append(value != null ? value : "").append("`\n\n");
            }
        }
    }

    private void renderCommonLabelsSection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 🏷️ Common Labels\n\n");

        for (Map<String, Object> row : rows) {
            String item = (String) row.get("item");
            String details = (String) row.get("details");

            if (item != null && !item.isEmpty()) {
                md.append("> ").append(item).append("\n\n");
                if (details != null && !details.isEmpty()) {
                    md.append("*").append(details).append("*\n\n");
                }
            }
        }
    }

    private void renderLabelSetTree(StringBuilder md, List<Map<String, Object>> treeRows,
                                    List<Map<String, Object>> metricsRows) {
        md.append("## 📋 Metrics by Label Set\n\n");

        // Build a map of metric name to statistics for quick lookup
        Map<String, Map<String, String>> metricStats = new HashMap<>();
        for (Map<String, Object> metricRow : metricsRows) {
            String name = (String) metricRow.get("item");
            String details = (String) metricRow.get("details");
            if (name != null && !name.isEmpty() && details != null) {
                metricStats.put(name, parseDetailsToMap(details));
            }
        }

        md.append("```\n");

        // The tree rows contain the label set hierarchy with metrics at the leaves
        // We need to parse each row and add statistics when we encounter a metric name
        for (int i = 0; i < treeRows.size(); i++) {
            Map<String, Object> row = treeRows.get(i);
            String item = (String) row.get("item");

            if (item != null && !item.isEmpty()) {
                // Keep original with colors for display, use clean for parsing
                String cleanItem = stripAnsiCodes(item);

                // Check if this line contains a metric name (at leaf level)
                // Metric names will be at the end after tree characters
                String metricName = extractMetricName(cleanItem);

                if (metricName != null && metricStats.containsKey(metricName)) {
                    // This is a metric leaf - add the metric name (with colors)
                    md.append(item).append("\n");

                    // Add statistics in map form below
                    Map<String, String> stats = metricStats.get(metricName);

                    // Determine if this metric has siblings after it by checking next row's indent
                    boolean hasMoreSiblings = hasMoreSiblingsAfter(treeRows, i, cleanItem);
                    String indent = calculateIndent(cleanItem);
                    String treePrefix = hasMoreSiblings ? "│   " : "    ";

                    // Colorize tree continuation characters
                    String colorizedTreePrefix = AnsiColors.colorizeTreeBranch(treePrefix);

                    // Format statistics as a map with tree continuation
                    for (Map.Entry<String, String> entry : stats.entrySet()) {
                        // Colorize property key and value
                        String colorizedKey = AnsiColors.colorize(entry.getKey(), AnsiColors.LABEL_KEY);
                        String colorizedValue = AnsiColors.colorize(entry.getValue(), AnsiColors.LABEL_VALUE);

                        md.append(indent).append(colorizedTreePrefix)
                          .append(colorizedKey).append(": ").append(colorizedValue).append("\n");
                    }
                } else {
                    // This is a label set node - render as-is (with colors)
                    md.append(item).append("\n");
                }
            }
        }

        md.append("```\n\n");
    }

    /**
     * Determine if a metric has more siblings after it at the same level.
     */
    private boolean hasMoreSiblingsAfter(List<Map<String, Object>> treeRows, int currentIndex, String currentLine) {
        if (currentIndex >= treeRows.size() - 1) {
            return false; // Last row, no siblings after
        }

        // Get the indent level of current line
        int currentIndent = getIndentLevel(currentLine);

        // Check if current line uses └── (last child marker)
        if (currentLine.contains("└──")) {
            return false;
        }

        // Check if current line uses ├── (has siblings marker)
        if (currentLine.contains("├──")) {
            return true;
        }

        // For lines without clear markers, check next lines
        for (int i = currentIndex + 1; i < treeRows.size(); i++) {
            String nextItem = (String) treeRows.get(i).get("item");
            if (nextItem == null || nextItem.isEmpty()) {
                continue;
            }
            String cleanNext = stripAnsiCodes(nextItem);
            int nextIndent = getIndentLevel(cleanNext);

            if (nextIndent < currentIndent) {
                // Next line is at a shallower level, no more siblings
                return false;
            } else if (nextIndent == currentIndent) {
                // Next line is at same level, has siblings
                return true;
            }
            // nextIndent > currentIndent means we're still in children, keep checking
        }

        return false;
    }

    /**
     * Get the indent level of a line by counting leading spaces before first non-space character.
     */
    private int getIndentLevel(String line) {
        if (line == null) {
            return 0;
        }
        int level = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                level++;
            } else {
                break;
            }
        }
        return level;
    }

    /**
     * Parse details string into a map of key-value pairs.
     * Input: "type=COUNTER, samples=135, label_sets=27, range=[600.0, 11010.0] operations"
     * Output: {type: COUNTER, samples: 135, label_sets: 27, range: [600.0, 11010.0], unit: operations}
     */
    private Map<String, String> parseDetailsToMap(String details) {
        Map<String, String> result = new LinkedHashMap<>();
        if (details == null || details.isEmpty()) {
            return result;
        }

        // Split by commas, but not inside brackets
        String[] parts = details.split(",\\s*(?![^\\[]*\\])");

        for (String part : parts) {
            // Check if this part contains a range with unit after it
            if (part.startsWith("range=")) {
                // Split on first space after the closing bracket
                int bracketEnd = part.indexOf(']');
                if (bracketEnd != -1 && bracketEnd + 1 < part.length()) {
                    String rangePart = part.substring(0, bracketEnd + 1);
                    String unitPart = part.substring(bracketEnd + 1).trim();

                    // Add range
                    String[] rangeKv = rangePart.split("=", 2);
                    if (rangeKv.length == 2) {
                        result.put(rangeKv[0].trim(), rangeKv[1].trim());
                    }

                    // Add unit if present
                    if (!unitPart.isEmpty()) {
                        result.put("unit", unitPart);
                    }
                } else {
                    // No unit, just range
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2) {
                        result.put(kv[0].trim(), kv[1].trim());
                    }
                }
            } else {
                // Normal key=value pair
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    result.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        return result;
    }

    /**
     * Calculate the full indent prefix from a tree line up to (but not including) the metric name.
     * This includes leading spaces AND intermediate tree structure (│, ├, └, ─, spaces).
     *
     * For example, given "        │   ├── metric_name", returns "        │   "
     */
    private String calculateIndent(String line) {
        if (line == null) {
            return "";
        }

        // Find the position of ├── or └── which marks the start of the metric name
        int treeMarkerPos = -1;
        if (line.contains("├── ")) {
            treeMarkerPos = line.indexOf("├── ");
        } else if (line.contains("└── ")) {
            treeMarkerPos = line.indexOf("└── ");
        }

        if (treeMarkerPos != -1) {
            // Return everything up to (but not including) the tree marker
            return line.substring(0, treeMarkerPos);
        }

        // Fallback: return just leading spaces
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                indent++;
            } else {
                break;
            }
        }
        return " ".repeat(Math.max(0, indent));
    }

    /**
     * Strip ANSI color codes from a string.
     */
    private String stripAnsiCodes(String input) {
        if (input == null) {
            return "";
        }
        // ANSI escape sequences: ESC[...m
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * Extract metric name from a tree line.
     * Metric names appear after tree characters (├──, └──, │) and are typically at the deepest indent.
     * They don't contain = signs (those are in label sets).
     */
    private String extractMetricName(String line) {
        if (line == null) {
            return null;
        }

        // Pattern to match text after tree characters
        // Metric names are words/underscores without = signs
        Pattern metricPattern = Pattern.compile("[├└]──\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*$");
        Matcher matcher = metricPattern.matcher(line);

        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            // Make sure this line doesn't have = signs (which would indicate a label set, not a metric)
            if (!line.contains("=")) {
                return candidate;
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return "metrics-markdown";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }
}

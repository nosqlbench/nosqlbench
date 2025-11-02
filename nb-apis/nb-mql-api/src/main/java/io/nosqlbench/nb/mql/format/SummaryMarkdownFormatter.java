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

import java.util.*;

/**
 * Custom markdown formatter for summary command output.
 * Creates a nicely structured report with tree views and sections.
 */
public class SummaryMarkdownFormatter implements ResultFormatter {

    @Override
    public String format(QueryResult result) {
        if (result.isEmpty()) {
            return "*No data available.*\n";
        }

        StringBuilder md = new StringBuilder();

        // Title
        md.append("# NoSQLBench Session Summary\n\n");

        // Group rows by section
        Map<String, List<Map<String, Object>>> sections = new LinkedHashMap<>();
        for (Map<String, Object> row : result.rows()) {
            String section = (String) row.get("section");
            sections.computeIfAbsent(section, k -> new ArrayList<>()).add(row);
        }

        // Render each section
        for (Map.Entry<String, List<Map<String, Object>>> entry : sections.entrySet()) {
            String sectionName = entry.getKey();
            List<Map<String, Object>> sectionRows = entry.getValue();

            if ("SESSION".equals(sectionName)) {
                renderSessionSection(md, sectionRows);
            } else if ("CORE METRICS".equals(sectionName)) {
                renderCoreMetricsSection(md, sectionRows);
            } else if ("ACTIVITY".equals(sectionName)) {
                renderActivitySection(md, sectionRows);
            } else if ("ERRORS".equals(sectionName)) {
                renderErrorsSection(md, sectionRows);
            } else if ("ALL METRICS".equals(sectionName)) {
                renderAllMetricsTree(md, sectionRows, sections.getOrDefault("LABEL SET", new ArrayList<>()));
            } else if ("LABEL SET".equals(sectionName)) {
                // Skip - rendered with ALL METRICS
                continue;
            } else {
                renderGenericSection(md, sectionName, sectionRows);
            }

            md.append("\n");
        }

        // Footer
        md.append("---\n\n");
        md.append(String.format("*Report generated in %dms*\n", result.executionTimeMs()));

        return md.toString();
    }

    private void renderSessionSection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 📊 Session Overview\n\n");

        for (Map<String, Object> row : rows) {
            String metric = (String) row.get("metric");
            String value = (String) row.get("value");
            String details = (String) row.get("details");

            md.append("**").append(metric).append(":**  \n");
            md.append("`").append(value).append("`");
            if (details != null && !details.isEmpty()) {
                md.append(" — *").append(details).append("*");
            }
            md.append("\n\n");
        }
    }

    private void renderCoreMetricsSection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 🎯 Core Metrics\n\n");

        md.append("| Metric | Value | Details |\n");
        md.append("|--------|-------|----------|\n");

        for (Map<String, Object> row : rows) {
            String metric = (String) row.get("metric");
            String value = (String) row.get("value");
            String details = (String) row.get("details");

            md.append("| **").append(metric).append("** | ");
            md.append("`").append(value).append("` | ");
            md.append(details != null ? details : "");
            md.append(" |\n");
        }

        md.append("\n");
    }

    private void renderActivitySection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 📈 Activity Breakdown\n\n");

        md.append("```\n");
        long total = rows.stream()
            .mapToLong(row -> {
                String value = (String) row.get("value");
                return (long) Double.parseDouble(value);
            })
            .sum();

        for (Map<String, Object> row : rows) {
            String activity = (String) row.get("metric");
            String value = (String) row.get("value");
            long count = (long) Double.parseDouble(value);
            double percentage = (count * 100.0) / total;

            md.append(String.format("%-15s %,12d ops  (%5.1f%%)\n",
                activity + ":", count, percentage));
        }

        md.append(String.format("%-15s %,12d ops\n", "TOTAL:", total));
        md.append("```\n\n");
    }

    private void renderErrorsSection(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## ⚠️ Errors\n\n");

        if (rows.isEmpty()) {
            md.append("*No errors recorded.*\n\n");
            return;
        }

        md.append("| Error Type | Count |\n");
        md.append("|------------|-------|\n");

        for (Map<String, Object> row : rows) {
            String metric = (String) row.get("metric");
            String value = (String) row.get("value");

            md.append("| `").append(metric).append("` | ");
            md.append("**").append(value).append("** |\n");
        }

        md.append("\n");
    }

    private void renderAllMetricsTree(StringBuilder md, List<Map<String, Object>> rows, List<Map<String, Object>> labelSetRows) {
        md.append("## 📋 Metrics Inventory\n\n");

        // View 1: Metrics with their label sets
        md.append("### View by Metric\n\n");
        md.append("```\n");

        // Group label sets by metric
        Map<String, List<String>> metricToLabelSets = new LinkedHashMap<>();
        for (Map<String, Object> labelRow : labelSetRows) {
            String metricName = (String) labelRow.get("metric");
            String labels = (String) labelRow.get("value");
            metricToLabelSets.computeIfAbsent(metricName, k -> new ArrayList<>()).add(labels);
        }

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            String name = (String) row.get("metric");
            String details = (String) row.get("details");

            boolean isLast = (i == rows.size() - 1);
            String prefix = isLast ? "└── " : "├── ";

            // Parse details to extract type, samples, label_sets, range
            Map<String, String> detailMap = parseDetails(details);
            String type = detailMap.getOrDefault("type", "UNKNOWN");
            String samples = detailMap.getOrDefault("samples", "0");
            String labelSets = detailMap.getOrDefault("label_sets", "0");
            String range = detailMap.getOrDefault("range", "[]");

            // Format metric name with aligned columns
            md.append(String.format("%-4s%-30s type=%-10s samples=%-6s label_sets=%-4s range=%s\n",
                prefix, name, type, samples, labelSets, range));

            // Show actual label sets as sub-items
            List<String> metricLabelSets = metricToLabelSets.get(name);
            if (metricLabelSets != null && !metricLabelSets.isEmpty()) {
                String detailPrefix = isLast ? "    " : "│   ";
                for (int j = 0; j < metricLabelSets.size(); j++) {
                    boolean isLastLabel = (j == metricLabelSets.size() - 1);
                    String branchPrefix = isLastLabel ? "└── " : "├── ";
                    md.append(String.format("%s%s%s\n", detailPrefix, branchPrefix, metricLabelSets.get(j)));
                }
            }
        }

        md.append("```\n\n");

        // View 2: Label sets with metrics that use them
        if (!labelSetRows.isEmpty()) {
            md.append("### View by Label Set\n\n");
            md.append("```\n");

            // Check if rows are already in tree format (value column contains tree characters)
            boolean isPreformatted = labelSetRows.stream()
                .anyMatch(row -> {
                    String value = (String) row.get("value");
                    return value != null && (value.contains("├──") || value.contains("└──") || value.contains("│"));
                });

            if (isPreformatted) {
                // Rows are already formatted as a tree, render them as-is
                for (Map<String, Object> labelRow : labelSetRows) {
                    String value = (String) labelRow.get("value");
                    String metric = (String) labelRow.get("metric");

                    // If value contains tree structure, use it
                    if (value != null && !value.isEmpty()) {
                        md.append(value);
                        // Only append metric name if it's not empty and not already in the tree
                        if (metric != null && !metric.isEmpty() && !value.contains(metric)) {
                            md.append(metric);
                        }
                        md.append("\n");
                    }
                }
            } else {
                // Original grouping logic for flat label sets
                Map<String, List<String>> labelSetToMetrics = new LinkedHashMap<>();
                for (Map<String, Object> labelRow : labelSetRows) {
                    String metricName = (String) labelRow.get("metric");
                    String labels = (String) labelRow.get("value");
                    labelSetToMetrics.computeIfAbsent(labels, k -> new ArrayList<>()).add(metricName);
                }

                int labelSetIndex = 0;
                for (Map.Entry<String, List<String>> entry : labelSetToMetrics.entrySet()) {
                    String labelSet = entry.getKey();
                    List<String> metrics = entry.getValue();

                    boolean isLastLabelSet = (labelSetIndex == labelSetToMetrics.size() - 1);
                    String prefix = isLastLabelSet ? "└── " : "├── ";

                    md.append(String.format("%s%s\n", prefix, labelSet));

                    String detailPrefix = isLastLabelSet ? "    " : "│   ";
                    for (int i = 0; i < metrics.size(); i++) {
                        boolean isLastMetric = (i == metrics.size() - 1);
                        String branchPrefix = isLastMetric ? "└── " : "├── ";
                        md.append(String.format("%s%s%s\n", detailPrefix, branchPrefix, metrics.get(i)));
                    }

                    labelSetIndex++;
                }
            }

            md.append("```\n\n");
        }
    }

    private Map<String, String> parseDetails(String details) {
        Map<String, String> result = new HashMap<>();
        if (details == null || details.isEmpty()) {
            return result;
        }

        // Parse "type=COUNTER, samples=25, label_sets=5, range=[0.0, 11000.0]"
        String[] parts = details.split(", (?![^\\[]*\\])");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0], kv[1]);
            }
        }
        return result;
    }

    private void renderGenericSection(StringBuilder md, String sectionName, List<Map<String, Object>> rows) {
        md.append("## ").append(sectionName).append("\n\n");

        md.append("| Metric | Value | Details |\n");
        md.append("|--------|-------|----------|\n");

        for (Map<String, Object> row : rows) {
            String metric = (String) row.get("metric");
            String value = (String) row.get("value");
            String details = (String) row.get("details");

            md.append("| ").append(metric).append(" | ");
            md.append(value != null ? value : "").append(" | ");
            md.append(details != null ? details : "");
            md.append(" |\n");
        }

        md.append("\n");
    }

    @Override
    public String getName() {
        return "summary-markdown";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }
}

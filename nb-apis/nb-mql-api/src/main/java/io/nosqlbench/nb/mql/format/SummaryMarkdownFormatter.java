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
                renderAllMetricsTree(md, sectionRows);
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

    private void renderAllMetricsTree(StringBuilder md, List<Map<String, Object>> rows) {
        md.append("## 📋 Metrics Inventory\n\n");

        md.append("```\n");
        md.append("Metrics Database\n");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            String name = (String) row.get("metric");
            String details = (String) row.get("details");

            boolean isLast = (i == rows.size() - 1);
            String prefix = isLast ? "└── " : "├── ";

            md.append(prefix).append(name).append("\n");

            if (details != null && !details.isEmpty()) {
                String detailPrefix = isLast ? "    " : "│   ";
                // Parse details and format as tree branches
                // Handle range=[min, max] specially to keep it on one line
                String[] parts = details.split(", (?![^\\[]*\\])");
                for (int j = 0; j < parts.length; j++) {
                    boolean isLastDetail = (j == parts.length - 1);
                    String branchPrefix = isLastDetail ? "└── " : "├── ";
                    md.append(detailPrefix).append(branchPrefix).append(parts[j]).append("\n");
                }
            }
        }

        md.append("```\n\n");
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

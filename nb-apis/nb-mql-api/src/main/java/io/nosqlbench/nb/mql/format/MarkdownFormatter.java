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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Formats query results as GitHub-flavored Markdown tables.
 * Output can be directly copy-pasted into markdown documentation.
 */
public class MarkdownFormatter implements ResultFormatter {

    private static final int DEFAULT_MAX_COLUMN_WIDTH = 50;

    @Override
    public String format(QueryResult result) {
        if (result.isEmpty()) {
            return "*No results found.*\n";
        }

        List<String> columns = result.columns();
        List<Map<String, Object>> rows = result.rows();

        // Calculate column widths
        Map<String, Integer> columnWidths = calculateColumnWidths(columns, rows);

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("| ");
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            sb.append(padRight(column, columnWidths.get(column)));
            if (i < columns.size() - 1) {
                sb.append(" | ");
            }
        }
        sb.append(" |\n");

        // Separator row
        sb.append("|");
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            sb.append("-".repeat(columnWidths.get(column) + 2)); // +2 for padding spaces
            if (i < columns.size() - 1) {
                sb.append("|");
            }
        }
        sb.append("|\n");

        // Data rows
        for (Map<String, Object> row : rows) {
            sb.append("| ");
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                Object value = row.get(column);
                String strValue = value != null ? escapeMarkdown(value.toString()) : "";

                // Truncate if too long
                if (strValue.length() > DEFAULT_MAX_COLUMN_WIDTH) {
                    strValue = strValue.substring(0, DEFAULT_MAX_COLUMN_WIDTH - 3) + "...";
                }

                sb.append(padRight(strValue, columnWidths.get(column)));
                if (i < columns.size() - 1) {
                    sb.append(" | ");
                }
            }
            sb.append(" |\n");
        }

        // Footer with metadata
        sb.append("\n");
        sb.append("*").append(result.rowCount()).append(" row");
        if (result.rowCount() != 1) {
            sb.append("s");
        }
        sb.append(" (").append(result.executionTimeMs()).append("ms)*\n");

        return sb.toString();
    }

    @Override
    public String getName() {
        return "markdown";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    private Map<String, Integer> calculateColumnWidths(List<String> columns, List<Map<String, Object>> rows) {
        Map<String, Integer> widths = new java.util.LinkedHashMap<>();

        // Initialize with header widths
        for (String column : columns) {
            widths.put(column, column.length());
        }

        // Check data widths
        for (Map<String, Object> row : rows) {
            for (String column : columns) {
                Object value = row.get(column);
                String strValue = value != null ? value.toString() : "";
                int currentWidth = widths.get(column);
                int valueWidth = Math.min(strValue.length(), DEFAULT_MAX_COLUMN_WIDTH);
                widths.put(column, Math.max(currentWidth, valueWidth));
            }
        }

        return widths;
    }

    private String padRight(String str, int width) {
        if (str.length() >= width) {
            return str;
        }
        return str + " ".repeat(width - str.length());
    }

    private String escapeMarkdown(String str) {
        // Escape pipe characters which would break the table
        return str.replace("|", "\\|");
    }
}

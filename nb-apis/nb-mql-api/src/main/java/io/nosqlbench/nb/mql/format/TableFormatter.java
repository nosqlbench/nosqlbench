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
 * Formats query results as ASCII tables suitable for console output.
 */
public class TableFormatter implements ResultFormatter {

    private static final int DEFAULT_MAX_COLUMN_WIDTH = 50;
    private static final String COLUMN_SEPARATOR = " | ";
    private static final String HEADER_SEPARATOR_CHAR = "-";

    @Override
    public String format(QueryResult result) {
        if (result.isEmpty()) {
            return "No results found.\n";
        }

        List<String> columns = result.columns();
        List<Map<String, Object>> rows = result.rows();

        // Calculate column widths
        Map<String, Integer> columnWidths = calculateColumnWidths(columns, rows);

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(formatRow(columns, columnWidths, columns));
        sb.append("\n");

        // Separator
        sb.append(formatSeparator(columns, columnWidths));
        sb.append("\n");

        // Data rows
        for (Map<String, Object> row : rows) {
            List<String> values = new ArrayList<>();
            for (String column : columns) {
                Object value = row.get(column);
                values.add(value != null ? value.toString() : "null");
            }
            sb.append(formatRow(values, columnWidths, columns));
            sb.append("\n");
        }

        // Footer with row count
        sb.append("\n");
        sb.append(result.rowCount()).append(" row");
        if (result.rowCount() != 1) {
            sb.append("s");
        }
        sb.append(" (").append(result.executionTimeMs()).append("ms)\n");

        return sb.toString();
    }

    @Override
    public String getName() {
        return "table";
    }

    @Override
    public String getFileExtension() {
        return "txt";
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
                String strValue = value != null ? value.toString() : "null";
                int currentWidth = widths.get(column);
                int valueWidth = Math.min(strValue.length(), DEFAULT_MAX_COLUMN_WIDTH);
                widths.put(column, Math.max(currentWidth, valueWidth));
            }
        }

        return widths;
    }

    private String formatRow(List<String> values, Map<String, Integer> columnWidths, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String value = values.get(i);
            int width = columnWidths.get(column);

            // Truncate if too long
            if (value.length() > DEFAULT_MAX_COLUMN_WIDTH) {
                value = value.substring(0, DEFAULT_MAX_COLUMN_WIDTH - 3) + "...";
            }

            // Pad to width
            sb.append(padRight(value, width));

            if (i < columns.size() - 1) {
                sb.append(COLUMN_SEPARATOR);
            }
        }
        return sb.toString();
    }

    private String formatSeparator(List<String> columns, Map<String, Integer> columnWidths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            int width = columnWidths.get(column);
            sb.append(HEADER_SEPARATOR_CHAR.repeat(width));

            if (i < columns.size() - 1) {
                sb.append("-+-");
            }
        }
        return sb.toString();
    }

    private String padRight(String str, int width) {
        if (str.length() >= width) {
            return str;
        }
        return str + " ".repeat(width - str.length());
    }
}

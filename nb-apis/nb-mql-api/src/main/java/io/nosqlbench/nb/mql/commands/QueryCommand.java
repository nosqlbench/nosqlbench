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

package io.nosqlbench.nb.mql.commands;

import io.nosqlbench.nb.mql.cli.QueryCLI;
import io.nosqlbench.nb.mql.parser.MetricsQLParseException;
import io.nosqlbench.nb.mql.parser.MetricsQLQueryParser;
import io.nosqlbench.nb.mql.parser.SQLFragment;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import picocli.CommandLine;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Query command - accepts arbitrary MetricsQL expressions and translates them to SQL.
 *
 * <p>This command uses the MetricsQL parser to accept full MetricsQL syntax including:</p>
 * <ul>
 *   <li>Selectors: {@code http_requests_total{job="api"}}</li>
 *   <li>Time ranges: {@code http_requests_total[5m]}</li>
 *   <li>Aggregations: {@code sum by(endpoint) (rate(http_requests[5m]))}</li>
 *   <li>Binary operations: {@code rate(errors[1m]) / rate(requests[1m])}</li>
 *   <li>Transforms: {@code clamp_min(rate(metric[5m]), 0)}</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * query "rate(http_requests[5m])"
 * query "sum by(endpoint) (latency)" --explain
 * query "http_requests_total{status='200'}" --db logs/metrics.db
 * }</pre>
 */
public class QueryCommand implements MetricsQueryCommand {

    private final MetricsQLQueryParser parser = new MetricsQLQueryParser();

    @Override
    public String getName() {
        return "query";
    }

    @Override
    public String getDescription() {
        return "Execute arbitrary MetricsQL queries";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String expression = (String) params.get("expression");
        long startTime = System.currentTimeMillis();

        // Parse and transform the MetricsQL expression to SQL
        SQLFragment sqlFragment;
        try {
            sqlFragment = parser.parse(expression);
        } catch (MetricsQLParseException e) {
            throw new InvalidQueryException("Failed to parse MetricsQL expression: " + e.getMessage(), e);
        }

        String sql = sqlFragment.getSql();
        List<Object> parameters = sqlFragment.getParameters();

        // Execute the query
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind parameters
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Get column names from result set metadata
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnName(i));
                }

                // Fetch rows
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(columns.get(i - 1), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return new QueryResult(columns, rows, sql, executionTime);
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        if (!params.containsKey("expression")) {
            throw new InvalidQueryException("Missing required parameter: expression");
        }

        Object expression = params.get("expression");
        if (!(expression instanceof String) || ((String) expression).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'expression' must be a non-empty MetricsQL expression");
        }

        // Validate the expression parses correctly
        String exprStr = (String) expression;
        try {
            parser.parse(exprStr);
        } catch (MetricsQLParseException e) {
            throw new InvalidQueryException("Invalid MetricsQL expression: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Simple selector
              query "http_requests_total"

              # Selector with labels
              query "http_requests_total{job='api', status='200'}"

              # Rate calculation
              query "rate(http_requests_total[5m])"

              # Aggregation
              query "sum by(endpoint) (rate(http_requests[5m]))"

              # Binary operation
              query "rate(errors[1m]) / rate(requests[1m])"

              # Show generated SQL instead of executing
              query "rate(http_requests[5m])" --explain

              # Specify database path
              query "sum(latency)" --db logs/metrics.db

              # Output in different format
              query "http_requests_total" --format json
            """;
    }

    /**
     * Standalone main method for direct execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryCLI()).execute(args);
        System.exit(exitCode);
    }
}

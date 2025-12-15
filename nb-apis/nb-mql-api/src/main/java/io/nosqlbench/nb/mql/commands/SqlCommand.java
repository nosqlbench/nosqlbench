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

import io.nosqlbench.nb.mql.cli.SqlCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL command - Execute raw SQLite queries against the metrics database.
 *
 * Usage: sql --query "SELECT * FROM metric_family"
 *
 * Allows advanced users to write custom SQL queries for exploration,
 * debugging, and analytics not covered by high-level commands.
 *
 * Safety:
 * - Connection is read-only (no modifications possible)
 * - Query timeout prevents runaway queries
 * - Full SQLite dialect and functions available
 */
public class SqlCommand implements MetricsQueryCommand {

    private static final int DEFAULT_QUERY_TIMEOUT_SECONDS = 30;

    @Override
    public String getName() {
        return "sql";
    }

    @Override
    public String getDescription() {
        return "Execute raw SQLite queries (read-only)";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String sql = (String) params.get("query");
        Integer timeoutSeconds = (Integer) params.getOrDefault("timeout", DEFAULT_QUERY_TIMEOUT_SECONDS);

        long startTime = System.currentTimeMillis();

        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setQueryTimeout(timeoutSeconds);

            try (ResultSet rs = ps.executeQuery()) {
                // Extract column names from result set metadata
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metadata.getColumnName(i));
                }

                // Extract rows
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metadata.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
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
        if (!params.containsKey("query")) {
            throw new InvalidQueryException("Missing required parameter: query");
        }

        Object query = params.get("query");
        if (!(query instanceof String) || ((String) query).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'query' must be a non-empty SQL string");
        }

        String sql = ((String) query).trim().toUpperCase();

        // Basic validation: ensure it's a SELECT query (read-only)
        if (!sql.startsWith("SELECT") && !sql.startsWith("WITH") && !sql.startsWith("PRAGMA")) {
            throw new InvalidQueryException(
                "Only SELECT, WITH (CTE), and PRAGMA queries are allowed.\n" +
                "Database is read-only to prevent modifications."
            );
        }

        // Block dangerous pragmas
        if (sql.contains("PRAGMA WRITABLE_SCHEMA") || sql.contains("PRAGMA CASE_SENSITIVE_LIKE")) {
            throw new InvalidQueryException("This PRAGMA is not allowed");
        }

        // Validate timeout if present
        if (params.containsKey("timeout")) {
            Object timeout = params.get("timeout");
            if (!(timeout instanceof Integer) || (Integer) timeout <= 0) {
                throw new InvalidQueryException("Parameter 'timeout' must be a positive integer (seconds)");
            }
        }
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # List all metric families
              sql --query "SELECT name, type, unit FROM metric_family ORDER BY name"

              # Get sample counts by metric
              sql --query "SELECT sn.sample, COUNT(*) as count FROM metric_instance mi JOIN sample_name sn ON mi.sample_name_id = sn.id JOIN sample_value sv ON sv.metric_instance_id = mi.id GROUP BY sn.sample"

              # Custom time-series query
              sql --query "SELECT datetime(sv.timestamp_ms/1000, 'unixepoch') as time, sv.value FROM metric_instance mi JOIN sample_value sv ON sv.metric_instance_id = mi.id WHERE mi.sample_name_id = 1 ORDER BY sv.timestamp_ms"

              # Schema inspection
              sql --query "PRAGMA table_info(sample_value)"

              # With timeout
              sql --query "SELECT * FROM sample_value" --timeout 60
            """;
    }

    /**
     * Standalone main method for direct execution of SQL queries.
     * Delegates to SqlCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.SqlCommand --query "SELECT ..."
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SqlCLI()).execute(args);
        System.exit(exitCode);
    }
}

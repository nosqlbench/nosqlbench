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

import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsSchema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Metadata command - Query session metadata from label_metadata table.
 *
 * Usage: metadata [--session session_name]
 *
 * Displays session metadata including NoSQLBench version, command-line arguments,
 * and hardware information stored during the session.
 */
public class MetadataCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "metadata";
    }

    @Override
    public String getDescription() {
        return "Display session metadata (version, command-line, hardware) from label_metadata table";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        long startTime = System.currentTimeMillis();

        // Build SQL to query all metadata
        String sql = """
            SELECT
              ls.hash AS label_set,
              lm.metadata_key,
              lm.metadata_value
            FROM label_metadata lm
            JOIN label_set ls ON ls.id = lm.label_set_id
            ORDER BY ls.hash, lm.metadata_key
            """;

        List<String> columns = new ArrayList<>();
        columns.add("label_set");
        columns.add("metadata_key");
        columns.add("metadata_value");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label_set", rs.getString("label_set"));
                row.put("metadata_key", rs.getString("metadata_key"));
                row.put("metadata_value", rs.getString("metadata_value"));
                rows.add(row);
            }
        } catch (SQLException e) {
            // If label_metadata table doesn't exist (older database format),
            // return empty result with helpful message
            if (e.getMessage().contains("no such table: label_metadata")) {
                // Return empty result - backward compatible with older databases
                long executionTime = System.currentTimeMillis() - startTime;
                return new QueryResult(columns, rows, sql + "\n-- Note: label_metadata table not found (database created before metadata feature)", executionTime);
            }
            throw e;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return new QueryResult(columns, rows, sql, executionTime);
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        // No required parameters
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Show all session metadata
              metadata

              # JSON format for scripting
              metadata --format json

            Typical output includes:
              - nb.version: NoSQLBench version number
              - nb.commandline: Full command-line invocation
              - nb.hardware: CPU, memory, network summary

            SQL Query Equivalent:
              SELECT
                ls.hash AS label_set,
                lm.metadata_key,
                lm.metadata_value
              FROM label_metadata lm
              JOIN label_set ls ON ls.id = lm.label_set_id
              ORDER BY ls.hash, lm.metadata_key
            """;
    }
}

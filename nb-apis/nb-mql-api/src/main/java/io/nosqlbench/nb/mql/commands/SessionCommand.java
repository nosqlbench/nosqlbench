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

import io.nosqlbench.nb.mql.cli.SessionCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.query.TimeWindowParser;
import io.nosqlbench.nb.mql.schema.MetricsSchema;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Session command - Display session time information.
 *
 * Usage: session
 *
 * Shows timing information about the metrics session including first/last snapshots,
 * duration, and snapshot cadence. Useful for understanding the data timeline.
 */
public class SessionCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public String getDescription() {
        return "Display session time information and snapshot cadence";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        long startTime = System.currentTimeMillis();

        String sql = """
            SELECT
              MIN(timestamp_ms) as first_snapshot_ms,
              MAX(timestamp_ms) as last_snapshot_ms,
              COUNT(DISTINCT timestamp_ms) as total_snapshots,
              COUNT(*) as total_samples
            FROM sample_value
            """;

        List<String> columns = new ArrayList<>();
        columns.add("first_snapshot");
        columns.add("last_snapshot");
        columns.add("duration");
        columns.add("total_snapshots");
        columns.add("total_samples");
        columns.add("avg_interval");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                long firstMs = rs.getLong("first_snapshot_ms");
                long lastMs = rs.getLong("last_snapshot_ms");
                int totalSnapshots = rs.getInt("total_snapshots");
                int totalSamples = rs.getInt("total_samples");

                long durationMs = lastMs - firstMs;
                long avgIntervalMs = totalSnapshots > 1 ? durationMs / (totalSnapshots - 1) : 0;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("first_snapshot", new Timestamp(firstMs).toString());
                row.put("last_snapshot", new Timestamp(lastMs).toString());
                row.put("duration", formatDuration(durationMs));
                row.put("total_snapshots", totalSnapshots);
                row.put("total_samples", totalSamples);
                row.put("avg_interval", formatDuration(avgIntervalMs));

                rows.add(row);
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return new QueryResult(columns, rows, sql, executionTime);
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        // No parameters required for session command
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Show session information
              session

              # JSON format for scripting
              session --format json

              # Markdown for documentation
              session --format markdown
            """;
    }

    private String formatDuration(long millis) {
        if (millis == 0) {
            return "0s";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Standalone main method for direct execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SessionCLI()).execute(args);
        System.exit(exitCode);
    }
}

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

package io.nosqlbench.nb.mql.cli;

import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.format.ResultFormatter;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Abstract base class for all query command CLIs.
 * Provides common functionality for database connection, formatting, and error handling.
 *
 * Subclasses must implement:
 * - createCommand() - Create the specific MetricsQueryCommand instance
 * - buildParams() - Build the parameter map for the command
 */
public abstract class AbstractQueryCommandCLI implements Callable<Integer> {

    @Option(
        names = {"-d", "--db"},
        description = "Path to metrics database (default: logs/metrics.db)",
        defaultValue = "logs/metrics.db"
    )
    protected Path databasePath;

    @Option(
        names = {"-m", "--metric"},
        description = "Metric name to query",
        required = true
    )
    protected String metric;

    @Option(
        names = {"-l", "--labels"},
        description = "Label filters as key=value,key=value",
        split = ","
    )
    protected Map<String, String> labels = new LinkedHashMap<>();

    @Option(
        names = {"-f", "--format"},
        description = "Output format: table, json, jsonl, csv, tsv, markdown",
        defaultValue = "table"
    )
    protected OutputFormat format = OutputFormat.TABLE;

    @Option(
        names = {"--explain"},
        description = "Show the SQL query instead of executing"
    )
    protected boolean explain;

    @Override
    public Integer call() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(databasePath)) {
            MetricsQueryCommand command = createCommand();
            Map<String, Object> params = buildParams();

            QueryResult result = command.execute(conn, params);

            if (explain) {
                System.out.println("SQL Query:");
                System.out.println(result.executedSQL());
                return 0;
            }

            ResultFormatter formatter = format.createFormatter();
            System.out.println(formatter.format(result));

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (System.getenv("DEBUG") != null) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    /**
     * Create the specific command instance for this CLI.
     *
     * @return MetricsQueryCommand instance
     */
    protected abstract MetricsQueryCommand createCommand();

    /**
     * Build the parameter map for the command.
     * Subclasses override to add command-specific parameters.
     *
     * @return Parameter map
     */
    protected Map<String, Object> buildParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("metric", metric);
        if (!labels.isEmpty()) {
            params.put("labels", labels);
        }
        return params;
    }
}

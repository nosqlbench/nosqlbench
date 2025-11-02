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

import io.nosqlbench.nb.mql.commands.QueryCommand;
import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * CLI for query command - executes arbitrary MetricsQL expressions.
 */
@Command(
    name = "query",
    description = "Execute arbitrary MetricsQL queries",
    mixinStandardHelpOptions = true
)
public class QueryCLI implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "MetricsQL expression to execute (e.g., 'rate(http_requests[5m])')"
    )
    private String expression;

    @Option(
        names = {"-d", "--db"},
        description = "Path to metrics database (default: logs/metrics.db)",
        defaultValue = "logs/metrics.db"
    )
    private Path databasePath;

    @Option(
        names = {"-f", "--format"},
        description = "Output format: table, json, jsonl, csv, tsv, markdown",
        defaultValue = "table"
    )
    private OutputFormat format = OutputFormat.TABLE;

    @Option(
        names = {"--explain"},
        description = "Show the SQL query instead of executing"
    )
    private boolean explain = false;

    @Override
    public Integer call() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(databasePath)) {
            QueryCommand command = new QueryCommand();

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("expression", expression);

            QueryResult result = command.execute(conn, params);

            if (explain) {
                System.out.println("-- Generated SQL from MetricsQL expression:");
                System.out.println("-- " + expression);
                System.out.println();
                System.out.println(result.executedSQL());
            } else {
                System.out.println(format.createFormatter().format(result));
            }

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
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryCLI()).execute(args);
        System.exit(exitCode);
    }
}

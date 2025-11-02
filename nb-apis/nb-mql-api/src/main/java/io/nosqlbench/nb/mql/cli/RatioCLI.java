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

import io.nosqlbench.nb.mql.commands.RatioCommand;
import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.format.ResultFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * CLI for ratio query command.
 * Calculates ratio between two metrics (useful for error rates).
 */
@Command(
    name = "ratio",
    description = "Calculate ratio between two metrics (numerator / denominator)",
    mixinStandardHelpOptions = true
)
public class RatioCLI implements Callable<Integer> {

    @Option(
        names = {"-d", "--db"},
        description = "Path to metrics database (default: logs/metrics.db)",
        defaultValue = "logs/metrics.db"
    )
    private Path databasePath;

    @Option(
        names = {"-n", "--numerator"},
        description = "Numerator metric name",
        required = true
    )
    private String numerator;

    @Option(
        names = {"--denominator"},
        description = "Denominator metric name",
        required = true
    )
    private String denominator;

    @Option(
        names = {"-l", "--labels"},
        description = "Label filters (applied to both metrics)",
        split = ","
    )
    private Map<String, String> labels = new LinkedHashMap<>();

    @Option(
        names = {"-w", "--window"},
        description = "Time window (e.g., 5m, 1h)"
    )
    private String window;

    @Option(
        names = {"--last"},
        description = "Alternative to --window"
    )
    private String last;

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
    private boolean explain;

    @Override
    public Integer call() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(databasePath)) {
            RatioCommand command = new RatioCommand();

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("numerator", numerator);
            params.put("denominator", denominator);

            if (!labels.isEmpty()) {
                params.put("labels", labels);
            }

            if (window != null) {
                params.put("window", window);
            } else if (last != null) {
                params.put("last", last);
            }

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
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new RatioCLI()).execute(args);
        System.exit(exitCode);
    }
}

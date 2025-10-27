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

import io.nosqlbench.nb.mql.commands.SummaryCommand;
import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.format.ResultFormatter;
import io.nosqlbench.nb.mql.format.SummaryMarkdownFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * CLI for summary command.
 * Displays database overview and metrics inventory.
 */
@Command(
    name = "summary",
    description = "Display overview of metrics in the database",
    mixinStandardHelpOptions = true
)
public class SummaryCLI implements Callable<Integer> {

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

    @Override
    public Integer call() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(databasePath)) {
            SummaryCommand command = new SummaryCommand();
            QueryResult result = command.execute(conn, Map.of());

            // Use custom markdown formatter for summary command
            ResultFormatter formatter;
            if (format == OutputFormat.MARKDOWN) {
                formatter = new SummaryMarkdownFormatter();
            } else {
                formatter = format.createFormatter();
            }

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
        int exitCode = new CommandLine(new SummaryCLI()).execute(args);
        System.exit(exitCode);
    }
}

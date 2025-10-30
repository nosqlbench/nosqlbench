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

import io.nosqlbench.nb.mql.commands.MetricsCommand;
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
 * CLI for metrics command.
 * Lists all metrics with their label sets in tree view.
 */
@Command(
    name = "metrics",
    description = "List all metrics with their label sets in tree view",
    mixinStandardHelpOptions = true
)
public class MetricsCLI implements Callable<Integer> {

    @Option(
        names = {"-d", "--db"},
        description = "Path to metrics database (default: logs/metrics.db)",
        defaultValue = "logs/metrics.db"
    )
    private Path databasePath;

    @Option(
        names = {"--group-by"},
        description = "Group by: name (default) or labelset",
        defaultValue = "name"
    )
    private String groupBy;

    @Option(
        names = {"--keep-labels"},
        description = "Labels to keep (not elide) even if common. Use '*' for all labels, or comma/tab-separated label names (default: activity,session)",
        defaultValue = "activity,session"
    )
    private String keepLabels;

    @Option(
        names = {"--no-condense"},
        description = "Disable tree condensation. Shows each metric instance separately with full label sets instead of condensing siblings with common patterns (default: false, condensation enabled)",
        defaultValue = "false"
    )
    private boolean noCondense;

    @Option(
        names = {"-f", "--format"},
        description = "Output format: table, json, jsonl, csv, tsv, markdown",
        defaultValue = "table"
    )
    private OutputFormat format = OutputFormat.TABLE;

    @Override
    public Integer call() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(databasePath)) {
            MetricsCommand command = new MetricsCommand();

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("group-by", groupBy);
            params.put("keep-labels", keepLabels);
            params.put("condense", !noCondense); // invert flag: --no-condense means condense=false

            QueryResult result = command.execute(conn, params);

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
        int exitCode = new CommandLine(new MetricsCLI()).execute(args);
        System.exit(exitCode);
    }
}

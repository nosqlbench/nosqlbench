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

import io.nosqlbench.nb.mql.commands.MetadataCommand;
import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.format.ResultFormatter;
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
 * CLI for metadata command.
 * Displays session metadata (version, command-line, hardware).
 */
@Command(
    name = "metadata",
    description = "Display session metadata (version, command-line, hardware)",
    mixinStandardHelpOptions = true
)
public class MetadataCLI implements Callable<Integer> {

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
            MetadataCommand command = new MetadataCommand();
            QueryResult result = command.execute(conn, Map.of());

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
        int exitCode = new CommandLine(new MetadataCLI()).execute(args);
        System.exit(exitCode);
    }
}

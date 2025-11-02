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

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main entry point for the MetricsQL CLI tool.
 * Routes to subcommands for different query types.
 *
 * Usage:
 *   nb-metricsql instant --metric ops_total
 *   nb-metricsql range --metric ops_total --window 5m
 *   nb-metricsql rate --metric ops_total --window 5m
 *   ...
 */
@Command(
    name = "nb-metricsql",
    description = "MetricsQL query engine for NoSQLBench SQLite metrics databases",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    subcommands = {
        QueryCLI.class,
        SessionCLI.class,
        MetadataCLI.class,
        SummaryCLI.class,
        MetricsCLI.class,
        InstantCLI.class,
        RangeCLI.class,
        RateCLI.class,
        IncreaseCLI.class,
        QuantileCLI.class,
        AggregateCLI.class,
        RatioCLI.class,
        TopKCLI.class,
        SqlCLI.class
    }
)
public class MetricsQLCLI implements Runnable {

    @Override
    public void run() {
        // If no subcommand specified, show help
        CommandLine.usage(this, System.out);
    }

    /**
     * Main entry point for the standalone JAR.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MetricsQLCLI()).execute(args);
        System.exit(exitCode);
    }
}

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

import io.nosqlbench.nb.mql.commands.QuantileCommand;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;

/**
 * CLI for quantile query command.
 * Extracts percentile values from timer/summary metrics.
 */
@Command(
    name = "quantile",
    description = "Extract percentile values from timer/summary metrics",
    mixinStandardHelpOptions = true
)
public class QuantileCLI extends AbstractQueryCommandCLI {

    @Option(
        names = {"-p", "--percentile", "--quantile"},
        description = "Quantile value (0.0-1.0, e.g., 0.95 for p95)",
        required = true
    )
    private double quantile;

    @Option(
        names = {"-w", "--window"},
        description = "Time window (e.g., 5m, 1h) - optional, defaults to all data"
    )
    private String window;

    @Option(
        names = {"--last"},
        description = "Alternative to --window (e.g., last 5m)"
    )
    private String last;

    @Override
    protected MetricsQueryCommand createCommand() {
        return new QuantileCommand();
    }

    @Override
    protected Map<String, Object> buildParams() {
        Map<String, Object> params = super.buildParams();

        params.put("quantile", quantile);

        // Add time window if specified
        if (window != null) {
            params.put("window", window);
        } else if (last != null) {
            params.put("last", last);
        }

        return params;
    }

    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new QuantileCLI()).execute(args);
        System.exit(exitCode);
    }
}

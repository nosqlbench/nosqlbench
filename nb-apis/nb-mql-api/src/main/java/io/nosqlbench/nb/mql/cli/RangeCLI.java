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

import io.nosqlbench.nb.mql.commands.RangeCommand;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;

/**
 * CLI for range query command.
 * Can be run standalone: java -cp ... io.nosqlbench.nb.mql.cli.RangeCLI --metric ops_total --window 5m
 */
@Command(
    name = "range",
    description = "Retrieve time-series data over a time window",
    mixinStandardHelpOptions = true
)
public class RangeCLI extends AbstractQueryCommandCLI {

    @Option(
        names = {"-w", "--window"},
        description = "Time window (e.g., 5m, 1h, 30s)"
    )
    private String window;

    @Option(
        names = {"--last"},
        description = "Alternative to --window (e.g., last 5m)"
    )
    private String last;

    @Option(
        names = {"--start"},
        description = "Start timestamp in epoch milliseconds"
    )
    private Long start;

    @Option(
        names = {"--end"},
        description = "End timestamp in epoch milliseconds"
    )
    private Long end;

    @Override
    protected MetricsQueryCommand createCommand() {
        return new RangeCommand();
    }

    @Override
    protected Map<String, Object> buildParams() {
        Map<String, Object> params = super.buildParams();

        // Add time range parameters
        if (window != null) {
            params.put("window", window);
        } else if (last != null) {
            params.put("last", last);
        } else if (start != null && end != null) {
            params.put("start", start);
            params.put("end", end);
        }

        return params;
    }

    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new RangeCLI()).execute(args);
        System.exit(exitCode);
    }
}

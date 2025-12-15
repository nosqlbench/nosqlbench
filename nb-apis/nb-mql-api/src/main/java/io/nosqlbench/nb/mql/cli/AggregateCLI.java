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

import io.nosqlbench.nb.mql.commands.AggregateCommand;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CLI for aggregate query command.
 * Performs aggregations (sum, avg, max, min, count) with optional grouping.
 */
@Command(
    name = "agg",
    description = "Perform aggregations (sum, avg, max, min, count) on metrics",
    mixinStandardHelpOptions = true
)
public class AggregateCLI extends AbstractQueryCommandCLI {

    @Option(
        names = {"--func", "--function"},
        description = "Aggregate function: sum, avg, max, min, count",
        required = true
    )
    private String function;

    @Option(
        names = {"--by"},
        description = "Group by labels (comma-separated, e.g., activity,host)",
        split = ","
    )
    private List<String> groupBy;

    @Option(
        names = {"-w", "--window"},
        description = "Time window (e.g., 5m, 1h) - optional, defaults to latest snapshot"
    )
    private String window;

    @Option(
        names = {"--last"},
        description = "Alternative to --window"
    )
    private String last;

    @Override
    protected MetricsQueryCommand createCommand() {
        return new AggregateCommand();
    }

    @Override
    protected Map<String, Object> buildParams() {
        Map<String, Object> params = super.buildParams();

        params.put("function", function);

        if (groupBy != null && !groupBy.isEmpty()) {
            params.put("by", groupBy);
        }

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
        int exitCode = new CommandLine(new AggregateCLI()).execute(args);
        System.exit(exitCode);
    }
}

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

import io.nosqlbench.nb.mql.commands.IncreaseCommand;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;

/**
 * CLI for increase query command.
 * Calculates total increase in counter value over a time window.
 */
@Command(
    name = "increase",
    description = "Calculate total increase in counter over time window",
    mixinStandardHelpOptions = true
)
public class IncreaseCLI extends AbstractQueryCommandCLI {

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

    @Override
    protected MetricsQueryCommand createCommand() {
        return new IncreaseCommand();
    }

    @Override
    protected Map<String, Object> buildParams() {
        Map<String, Object> params = super.buildParams();

        // Add time window parameter
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
        int exitCode = new CommandLine(new IncreaseCLI()).execute(args);
        System.exit(exitCode);
    }
}

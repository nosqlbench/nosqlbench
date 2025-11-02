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

import io.nosqlbench.nb.mql.commands.InstantCommand;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Map;

/**
 * CLI for instant query command.
 * Can be run standalone: java -cp ... io.nosqlbench.nb.mql.cli.InstantCLI --metric ops_total
 */
@Command(
    name = "instant",
    description = "Get the most recent value for a metric",
    mixinStandardHelpOptions = true
)
public class InstantCLI extends AbstractQueryCommandCLI {

    @Override
    protected MetricsQueryCommand createCommand() {
        return new InstantCommand();
    }

    @Override
    protected Map<String, Object> buildParams() {
        // Use base class params (metric and labels)
        return super.buildParams();
    }

    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new InstantCLI()).execute(args);
        System.exit(exitCode);
    }
}

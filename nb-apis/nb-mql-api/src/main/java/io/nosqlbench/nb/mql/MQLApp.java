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

package io.nosqlbench.nb.mql;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.mql.cli.MetricsQLCLI;
import picocli.CommandLine;

/**
 * MetricsQL Query Engine as a NoSQLBench BundledApp.
 * Allows querying SQLite metrics databases using MetricsQL-inspired syntax.
 *
 * Usage:
 *   nb5 mql instant --metric ops_total
 *   nb5 mql range --metric ops_total --window 5m
 *
 * This integrates the MetricsQL CLI as a bundled application within NoSQLBench,
 * making it immediately available without needing a separate tool.
 */
@Service(value = BundledApp.class, selector = "mql")
public class MQLApp implements BundledApp {

    @Override
    public int applyAsInt(String[] args) {
        // Delegate to the picocli-based MetricsQLCLI
        // This allows all the command routing and validation to work seamlessly
        return new CommandLine(new MetricsQLCLI()).execute(args);
    }

    /**
     * Standalone main method for direct execution (optional).
     * Can also be run via: java -cp ... io.nosqlbench.nb.mql.MQLApp instant --metric ops
     */
    public static void main(String[] args) {
        MQLApp app = new MQLApp();
        int exitCode = app.applyAsInt(args);
        System.exit(exitCode);
    }
}

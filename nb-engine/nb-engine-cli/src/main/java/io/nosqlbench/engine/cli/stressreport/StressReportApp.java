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

package io.nosqlbench.engine.cli.stressreport;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/// Bundled app that emits a cassandra-stress-style summary from the metrics persisted at the
/// end of a NoSQLBench session.
///
/// **Sources.** Either the session SQLite database (default discovery: `./logs/metrics.db`
/// via the session symlink) or the CSV directory written by the CSV reporter. The same
/// underlying [SessionSummary] shape is produced from either source.
///
/// **A/B mode.** With `--ab --sqlite <db> --csv <dir>`, both sources are read, both reports
/// printed, and a per-field equivalence check is performed. Non-zero exit if any field
/// differs beyond tolerance — useful for verifying that the two reporters capture equivalent
/// information for the same session.
///
/// **Args.**
/// - `--sqlite <path>` — explicit SQLite database
/// - `--csv <dir>` — explicit CSV directory
/// - `--logs-dir <dir>` — discover `<dir>/metrics.db` (the symlink the session keeps current)
/// - `--ab` — A/B mode; requires both `--sqlite` and `--csv`
/// - (default) — discover `./logs/metrics.db`
@Command(name = "stress-report",
    description = "Emit a cassandra-stress-style summary from NoSQLBench session metrics.")
@Service(value = BundledApp.class, selector = "stress-report")
public class StressReportApp implements BundledApp {

    static final int EXIT_OK = 0;
    static final int EXIT_USAGE = 64;
    static final int EXIT_MISMATCH = 1;
    static final int EXIT_ERROR = 2;

    @Option(names = "--sqlite", paramLabel = "<db>",
        description = "Read from session SQLite database")
    private Path sqlitePath;

    @Option(names = "--csv", paramLabel = "<dir>",
        description = "Read from CSV reporter directory")
    private Path csvDir;

    @Option(names = "--logs-dir", paramLabel = "<dir>",
        description = "Discover <dir>/metrics.db (default: ./logs/metrics.db)")
    private Path logsDir;

    @Option(names = "--ab",
        description = "Read from BOTH sources and verify equivalence (requires --sqlite and --csv)")
    private boolean ab;

    @Option(names = {"--help", "-h"}, usageHelp = true, description = "Display help")
    private boolean helpRequested;

    private final PrintStream out;
    private final PrintStream err;

    public StressReportApp() {
        this(System.out, System.err);
    }

    /// Constructor for tests that want to capture stdout/stderr.
    public StressReportApp(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    /// Exposes the picocli model of this app so the `runapp` command-stream verb can adapt
    /// `name=value` parameters into the correct argv (e.g. `sqlite=x` -> `--sqlite=x`).
    @Override
    public Optional<CommandLine> getCommandModel() {
        return Optional.of(new CommandLine(this));
    }

    @Override
    public int applyAsInt(String[] args) {
        try {
            new CommandLine(this).parseArgs(args);
        } catch (CommandLine.ParameterException pe) {
            err.println(pe.getMessage());
            printUsage(err);
            return EXIT_USAGE;
        }
        if (helpRequested) {
            printUsage(out);
            return EXIT_OK;
        }

        if (ab) {
            if (sqlitePath == null || csvDir == null) {
                err.println("--ab requires both --sqlite and --csv");
                return EXIT_USAGE;
            }
            return runAb(new SqliteStressReportSource(sqlitePath),
                new CsvStressReportSource(csvDir));
        }

        StressReportSource source;
        if (sqlitePath != null) {
            source = new SqliteStressReportSource(sqlitePath);
        } else if (csvDir != null) {
            source = new CsvStressReportSource(csvDir);
        } else {
            Path base = (logsDir != null) ? logsDir : Path.of("logs");
            Path candidate = base.resolve("metrics.db");
            source = new SqliteStressReportSource(candidate);
        }
        return runSingle(source);
    }

    private int runSingle(StressReportSource source) {
        try {
            SessionSummary summary = source.readSummary();
            String text = new StressReportRenderer().render(summary, source.sourceDescription());
            out.print(text);
            return EXIT_OK;
        } catch (Exception e) {
            err.printf(Locale.US, "stress-report failed for %s: %s%n",
                source.sourceDescription(), e);
            return EXIT_ERROR;
        }
    }

    private int runAb(StressReportSource a, StressReportSource b) {
        SessionSummary sa;
        SessionSummary sb;
        try {
            sa = a.readSummary();
            sb = b.readSummary();
        } catch (Exception e) {
            err.printf(Locale.US, "stress-report A/B failed: %s%n", e);
            return EXIT_ERROR;
        }

        StressReportRenderer renderer = new StressReportRenderer();
        out.println("--- A ---");
        out.print(renderer.render(sa, a.sourceDescription()));
        out.println("--- B ---");
        out.print(renderer.render(sb, b.sourceDescription()));
        out.println("--- A/B comparison ---");

        StressReportComparator comparator = new StressReportComparator();
        StressReportComparator.ComparisonResult result = comparator.compare(sa, sb);
        out.print(comparator.renderResult(result, a.sourceDescription(), b.sourceDescription()));
        return result.isMatch() ? EXIT_OK : EXIT_MISMATCH;
    }

    private static void printUsage(PrintStream ps) {
        ps.println("""
            Usage: stress-report [--sqlite <db>] [--csv <dir>] [--logs-dir <dir>] [--ab]

              --sqlite <db>     Read from session SQLite database
              --csv <dir>       Read from CSV reporter directory
              --logs-dir <dir>  Discover <dir>/metrics.db (default: ./logs/metrics.db)
              --ab              Read from BOTH sources and verify equivalence
                                (requires both --sqlite and --csv; non-zero exit on mismatch)
            """);
    }
}

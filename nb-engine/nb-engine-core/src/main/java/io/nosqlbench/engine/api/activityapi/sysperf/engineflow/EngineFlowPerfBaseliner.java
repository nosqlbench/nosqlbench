/*
 * Copyright (c) 2025 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.sysperf.engineflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Convenience runner for engine-flow related JMH microbenchmarks.
 * <p>
 * JMH progress is shown on the console and simultaneously captured to a log file.
 * Logging is configured at INFO level during benchmarks to avoid performance degradation.
 * <p>
 * If you need full control over JMH options, use {@link org.openjdk.jmh.Main} directly.
 */
@Command(
    name = "EngineFlowPerfBaseliner",
    mixinStandardHelpOptions = true,
    description = "Run engine-flow JMH microbenchmarks with output capture.",
    version = "1.0"
)
public class EngineFlowPerfBaseliner implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(EngineFlowPerfBaseliner.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Option(
        names = {"-i", "--include"},
        description = "JMH benchmark include regex pattern.",
        defaultValue = ".*(engineflow\\..*Bench|StrideContextJmhBench).*"
    )
    private String include;

    @Option(
        names = {"-r", "--result"},
        description = "JMH JSON/CSV result output file (optional)."
    )
    private String resultFile;

    @Option(
        names = {"-l", "--log"},
        description = "File to capture JMH console output. Default: target/jmh-output-<timestamp>.log"
    )
    private String logFile;

    @Option(
        names = {"-f", "--forks"},
        description = "Number of JMH forks.",
        defaultValue = "1"
    )
    private int forks;

    @Option(
        names = {"--gc"},
        description = "Run GC between iterations.",
        defaultValue = "true"
    )
    private boolean shouldDoGC;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EngineFlowPerfBaseliner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        logger.info("starting EngineFlowPerfBaseliner");

        Path effectiveLogPath = resolveLogPath();
        String effectiveLogFile = effectiveLogPath.toString();

        logger.info("benchmark include regex: {}", include);
        if (resultFile == null) {
            logger.info("jmh results file: (none)");
        } else {
            logger.info("jmh results file: {}", resultFile);
        }
        logger.info("jmh output log: {}", effectiveLogFile);
        logger.info("forks: {}, gc: {}", forks, shouldDoGC);

        logger.info("building JMH options");
        OptionsBuilder builder = new OptionsBuilder();
        builder.forks(forks);
        builder.shouldFailOnError(true);
        builder.include(include);
        builder.shouldDoGC(shouldDoGC);
        // Use JMH's native output capture for forked processes
        builder.output(effectiveLogFile);
        if (resultFile != null) {
            builder.result(resultFile);
        }
        Options options = builder.build();

        logger.info("running JMH benchmarks");
        Collection<RunResult> results = runWithOutputTee(options, effectiveLogFile);
        logger.info("JMH run complete ({} result(s))", results.size());
        logger.info("JMH output saved to: {}", effectiveLogFile);

        return 0;
    }

    private Path resolveLogPath() throws IOException {
        String logFileName = (logFile != null && !logFile.isBlank())
            ? logFile
            : "target/jmh-output-" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + ".log";

        Path logPath = Path.of(logFileName);
        if (!logPath.isAbsolute()) {
            logPath = logPath.toAbsolutePath();
        }
        Files.createDirectories(logPath.getParent());
        return logPath;
    }

    /// Run JMH benchmarks while tailing the output file to console.
    /// JMH's output() option captures all output including from forked processes.
    /// We tail the file in a background thread to show real-time progress.
    private Collection<RunResult> runWithOutputTee(Options options, String logFile) throws Exception {
        Path logPath = Path.of(logFile);

        // Start a background thread to tail the log file to console
        Thread tailThread = new Thread(new FileTailer(logPath), "jmh-output-tailer");
        tailThread.setDaemon(true);
        tailThread.start();

        try {
            return new Runner(options).run();
        } finally {
            // Give the tailer a moment to catch up, then interrupt it
            Thread.sleep(500);
            tailThread.interrupt();
        }
    }

    /// Background task that tails a file and prints new content to stdout.
    private static class FileTailer implements Runnable {
        private final Path path;

        FileTailer(Path path) {
            this.path = path;
        }

        @Override
        public void run() {
            try {
                // Wait for file to be created
                while (!Files.exists(path) && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                }

                try (var reader = Files.newBufferedReader(path)) {
                    while (!Thread.currentThread().isInterrupted()) {
                        String line = reader.readLine();
                        if (line != null) {
                            System.out.println(line);
                        } else {
                            // No new content, wait a bit
                            Thread.sleep(50);
                        }
                    }
                    // Drain any remaining content
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // Log file access error, but don't fail the benchmark
                System.err.println("Warning: could not tail log file: " + e.getMessage());
            }
        }
    }
}

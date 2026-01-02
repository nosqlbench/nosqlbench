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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.components.core.NBComponentProps;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.components.decorators.NBTokenWords;
import io.nosqlbench.nb.api.components.status.NBHeartbeatComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.mql.commands.SummaryCommand;
import io.nosqlbench.nb.mql.format.OutputFormat;
import io.nosqlbench.nb.mql.format.ResultFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A session represents a single execution of NoSQLBench, whether episodic or persistent under some service layer.
 * An NBSession takes care of system level concerns like logging, annotations, error reporting, metrics flows, and so
 * on.
 * All NBScenarios are run within an NBSession.
 */
public class NBSession extends NBHeartbeatComponent implements Function<List<Cmd>, ExecutionResult>, NBTokenWords {
    private final static Logger logger = LogManager.getLogger(NBSession.class);
    //    private final ClientSystemMetricChecker clientMetricChecker;
    private MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
    private OperatingSystemMXBean osbean = ManagementFactory.getOperatingSystemMXBean();

    private final Map<String, NBBufferedContainer> containers = new ConcurrentHashMap<>();
    private final long sessionStartNanos;
    private SqliteSnapshotReporter sessionSqliteReporter;
    private Thread sessionSqliteShutdownHook;
    private static final long DEFAULT_SQLITE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);
    private Path sessionDbPath;
    private long sessionSqliteIntervalMillis = DEFAULT_SQLITE_INTERVAL_MS;

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName,
        Map<String, String> props
    ) {
        super(
            null,
            labelContext.getLabels()
                .andPairs("session", sessionName),
            props,
            "session"
        );

        this.sessionStartNanos = System.nanoTime();

        new NBSessionSafetyMetrics(this);

        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        if (osMxBean instanceof com.sun.management.UnixOperatingSystemMXBean osb) {
            create().gauge(
                "open_file_descriptors",
                () -> (double) osb.getOpenFileDescriptorCount(),
                MetricCategory.Internals,
                "open file descriptors"
            );
        }

        // on-heap
        create().gauge(
            "on_heap_memory_used",
            () -> (double) mbean.getHeapMemoryUsage().getUsed(),
            MetricCategory.Internals,
            "heap memory used for nb"
        );
        create().gauge(
            "on_heap_memory_max",
            () -> (double) mbean.getHeapMemoryUsage().getMax(),
            MetricCategory.Internals,
            "heap memory max for nb"
        );
        create().gauge(
            "on_heap_memory_committed",
            () -> (double) mbean.getHeapMemoryUsage().getCommitted(),
            MetricCategory.Internals,
            "heap memory committed for nb"
        );

        // off-heap
        create().gauge(
            "off_heap_memory_used",
            () -> (double) mbean.getNonHeapMemoryUsage().getUsed(),
            MetricCategory.Internals,
            "off-heap memory used for nb"
        );
        create().gauge(
            "off_heap_memory_max",
            () -> (double) mbean.getNonHeapMemoryUsage().getMax(),
            MetricCategory.Internals,
            "off-heap memory max for nb"
        );
        create().gauge(
            "off_heap_memory_committed",
            () -> (double) mbean.getNonHeapMemoryUsage().getCommitted(),
            MetricCategory.Internals,
            "off-heap memory committed for nb"
        );

        create().gauge(
            "session_time",
            () -> (double) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.sessionStartNanos),
            MetricCategory.Core,
            "elapsed session time in milliseconds"
        );

        initializeDefaultSqliteReporter();
        bufferOrphanedMetrics = true;
    }


    public ExecutionResult apply(List<Cmd> cmds) {

        // TODO: add container closing command
        // TODO: inject container closing commands after the last command referencing each container
        List<Cmd> assembledCommands = NBCommandAssembly.assemble(cmds, this::getContext);
        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector).ok()) {
            for (Cmd cmd : assembledCommands) {
                String explanation = "in container '" + cmd.getTargetContext() + "', command '" + cmd.toString() + "'";
                try (NBInvokableCommand command = NBCommandAssembly.resolve(cmd, this::getContext)) {
                    NBCommandParams params = NBCommandAssembly.paramsFor(cmd);
                    NBBufferedContainer container = getContext(cmd.getTargetContext());
                    NBCommandResult cmdResult = container.apply(command, params);
                    results.apply(cmdResult);
                    if (cmdResult.hasException()) {
                        throw cmdResult.getException();
                    }
                } catch (Exception e) {
                    String msg = "While running " + explanation + ", an error occurred: " + e.toString();
                    results.error(e);
                    onError(e);
                    logger.error(msg);
                    break;
                }
            }
        }

        for (String containerName : containers.keySet()) {
            NBBufferedContainer ctx = containers.get(containerName);
            logger.debug("awaiting end of activities in container '" + containerName + "':" +
                ctx.controller().getActivityDefs().stream().map(ActivityDef::getAlias).toList());
            ctx.controller().shutdown();
            ctx.controller().awaitCompletion(Long.MAX_VALUE);
            logger.debug("completed");
        }
        emitSqliteSummaryIfConfigured();
        return collector.toExecutionResult();
    }

    private void initializeDefaultSqliteReporter() {
        getComponentProp("logsdir").ifPresent(logsDirValue -> {
            Path logsDir = Path.of(logsDirValue);
            try {
                Files.createDirectories(logsDir);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create logs directory '" + logsDir + "'", e);
            }
            String sessionName = getLabels().valueOf("session");
            String sessionDbFilename = sessionName + "_metrics.db";
            Path sessionDbPath = logsDir.resolve(sessionDbFilename);
            this.sessionDbPath = sessionDbPath;
            this.sessionSqliteIntervalMillis = DEFAULT_SQLITE_INTERVAL_MS;
            setComponentProp("metricsdb", sessionDbPath.toAbsolutePath().toString());
            String jdbcUrl = "jdbc:sqlite:" + sessionDbPath.toAbsolutePath();

            MetricInstanceFilter filter = new MetricInstanceFilter();
            boolean includeHistograms = getComponentProp("metrics.sqlite.histograms")
                .map(Boolean::parseBoolean)
                .orElse(false);
            sessionSqliteReporter = create().sqliteSnapshotReporter(
                this,
                jdbcUrl,
                DEFAULT_SQLITE_INTERVAL_MS,
                filter,
                includeHistograms
            );

            // Send session metadata to the reporter
            sendSessionMetadata(sessionSqliteReporter);

            sessionSqliteShutdownHook = new Thread(() -> {
                try {
                    sessionSqliteReporter.close();
                } catch (Exception ignored) {
                }
            }, "sqlite-metrics-shutdown");

            try {
                Runtime.getRuntime().addShutdownHook(sessionSqliteShutdownHook);
            } catch (IllegalStateException ignored) {
                // if runtime is already shutting down, nothing to do
            }

            updateMetricsSymlink(logsDir, sessionDbFilename);
        });
    }

    private void updateMetricsSymlink(Path logsDir, String sessionDbFilename) {
        Path linkPath = logsDir.resolve("metrics.db");
        Path target = logsDir.resolve(sessionDbFilename);
        try {
            Files.deleteIfExists(linkPath);
            Files.createSymbolicLink(linkPath, target.getFileName());
        } catch (UnsupportedOperationException e) {
            logger.debug("Symbolic links are not supported; skipping metrics symlink creation.");
        } catch (IOException e) {
            logger.warn("Unable to update metrics symlink '{}' -> '{}'", linkPath, target, e);
        }
    }

    private void sendSessionMetadata(SqliteSnapshotReporter reporter) {
        Map<String, String> metadata = new LinkedHashMap<>();

        // Add NoSQLBench version
        getComponentProp("nb.version").ifPresent(version ->
            metadata.put("nb.version", version));

        // Add command-line
        getComponentProp("nb.commandline").ifPresent(cmdline ->
            metadata.put("nb.commandline", cmdline));

        // Add hardware/system info
        getComponentProp("nb.hardware").ifPresent(hardware ->
            metadata.put("nb.hardware", hardware));

        // Send metadata with session labels
        if (!metadata.isEmpty()) {
            reporter.onSessionMetadata(getLabels(), metadata);
        }
    }

    private void emitSqliteSummaryIfConfigured() {
        flushSqliteReporter();
        Optional<String> summarySpec = getComponentProp(NBComponentProps.SUMMARY);
        if (summarySpec.isEmpty()) {
            return;
        }
        Optional<String> summary = renderSummaryFromMetricsDb();
        if (summary.isEmpty()) {
            logger.warn("Skipping metrics summary because it could not be generated.");
            return;
        }
        List<SummaryTarget> targets = parseSummaryTargets(summarySpec.get());
        for (SummaryTarget target : targets) {
            PrintStream out = target.stream();
            try {
                out.print(summary.get());
                if (!summary.get().endsWith(System.lineSeparator())) {
                    out.print(System.lineSeparator());
                }
                out.flush();
            } catch (Exception e) {
                logger.warn("Unable to write metrics summary: {}", e.toString(), e);
            } finally {
                if (target.closeAfterWrite()) {
                    out.close();
                }
            }
        }
    }

    private Optional<String> renderSummaryFromMetricsDb() {
        if (sessionDbPath == null) {
            logger.warn("No metrics database path set; skipping MQL summary.");
            return Optional.empty();
        }
        if (!Files.exists(sessionDbPath)) {
            logger.warn("Metrics database '{}' does not exist; skipping MQL summary.", sessionDbPath);
            return Optional.empty();
        }
        try (Connection conn = MetricsDatabaseReader.connect(sessionDbPath)) {
            SummaryCommand command = new SummaryCommand();
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("keep-labels", "activity,session");
            params.put("condense", true);
            QueryResult result = command.execute(conn, params);
            ResultFormatter formatter = OutputFormat.TABLE.createFormatter();
            return Optional.of(formatter.format(result));
        } catch (Exception e) {
            logger.warn("Error rendering metrics summary from '{}': {}", sessionDbPath, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private List<SummaryTarget> parseSummaryTargets(String reportSummaryTo) {
        List<SummaryTarget> targets = new ArrayList<>();
        String[] destinationSpecs = reportSummaryTo.split(", *");
        for (String spec : destinationSpecs) {
            if (spec == null || spec.isBlank()) {
                continue;
            }
            String[] split = spec.split(":", 2);
            String summaryTo = split[0];
            try {
                switch (summaryTo.toLowerCase()) {
                    case "console":
                    case "stdout":
                        targets.add(new SummaryTarget(System.out, false));
                        break;
                    case "stderr":
                        targets.add(new SummaryTarget(System.err, false));
                        break;
                    default:
                        String outName = summaryTo
                            .replaceAll("_SESSION_", getLabels().valueOf("session"))
                            .replaceAll("_LOGS_", getComponentProp("logsdir").orElseThrow());
                        PrintStream out = new PrintStream(new FileOutputStream(outName));
                        targets.add(new SummaryTarget(out, true));
                }
            } catch (Exception e) {
                logger.warn("Unable to open summary destination '{}': {}", spec, e.toString());
            }
        }
        return targets;
    }

    private void flushSqliteReporter() {
        if (sessionSqliteReporter != null) {
            try {
                MetricsView snapshot = MetricsView.capture(find().metrics(), sessionSqliteIntervalMillis);
                sessionSqliteReporter.onMetricsSnapshot(snapshot);
            } catch (Exception e) {
                logger.warn("Unable to capture final metrics snapshot for SQLite reporter: {}", e.toString(), e);
            }
            try {
                sessionSqliteReporter.close();
            } catch (Exception e) {
                logger.warn("Error closing SQLite snapshot reporter: {}", e.toString(), e);
            } finally {
                sessionSqliteReporter = null;
            }
        }
        if (sessionSqliteShutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(sessionSqliteShutdownHook);
            } catch (IllegalStateException ignored) {
                // JVM is already shutting down
            }
            sessionSqliteShutdownHook = null;
        }
    }

    private record SummaryTarget(PrintStream stream, boolean closeAfterWrite) {
    }


    private NBBufferedContainer getContext(String name) {
        return containers.computeIfAbsent(
            name,
            n -> NBContainer.builder().name(n).build(this)
        );
    }

    @Override
    protected void teardown() {
        flushSqliteReporter();
        super.teardown();
    }

}

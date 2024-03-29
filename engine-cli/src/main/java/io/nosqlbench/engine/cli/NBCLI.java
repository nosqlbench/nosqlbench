/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.engine.cmdstream.CmdType;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Layer;
import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.util.Unit;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.logging.NBLogLevel;
import io.nosqlbench.nb.api.metadata.SessionNamer;
import io.nosqlbench.nb.api.metadata.SystemId;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogDumperUtility;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogImporterUtility;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.cli.NBCLIOptions.Mode;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityTypeLoader;
import io.nosqlbench.engine.core.lifecycle.session.NBSession;
import io.nosqlbench.engine.core.logging.NBLoggerConfig;
import io.nosqlbench.engine.core.metadata.MarkdownFinder;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.ServiceSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.ServiceLoader.Provider;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBCLI implements Function<String[], Integer>, NBLabeledElement {

    private static Logger logger;
    private static final NBLoggerConfig loggerConfig;
    private static final int EXIT_OK = 0;
    private static final int EXIT_WARNING = 1;
    private static final int EXIT_ERROR = 2;

    static {
        loggerConfig = new NBLoggerConfig();
        ConfigurationFactory.setConfigurationFactory(NBCLI.loggerConfig);
    }

    private final String commandName;
    private String sessionName;
    private String sessionCode;
    private long sessionTime;
    private NBLabels labels;


    public NBCLI(final String commandName) {
        this.commandName = commandName;
    }

    /**
     * Only call System.exit with the body of main. This is so that other scenario
     * invocations are handled functionally by {@link #apply(String[])}, which allows
     * for scenario encapsulation and concurrent testing.
     *
     * @param args
     *     Command Line Args
     */
    public static void main(final String[] args) {
        try {
            final NBCLI cli = new NBCLI("nb5");
            final int statusCode = cli.apply(args);
            System.exit(statusCode);
        } catch (final Exception e) {
            System.out.println("Not expected issue in main: " + e.getMessage());
        }
    }

    /**
     * return null;
     * }
     * <p>
     * public static void main(String[] args) {
     *
     * @param args
     * @return
     */
    @Override
    public Integer apply(final String[] args) {
        try {
            final NBCLI cli = new NBCLI("nb5");
            final int result = cli.applyDirect(args);
            return result;
        } catch (final Exception e) {
            boolean showStackTraces = false;
            for (final String arg : args)
                if (arg.toLowerCase(Locale.ROOT).startsWith("-v") || "--show-stacktraces".equals(arg.toLowerCase(Locale.ROOT))) {
                    showStackTraces = true;
                    break;
                }

            final String error = NBCLIErrorHandler.handle(e, showStackTraces);
            // Commented for now, as the above handler should do everything needed.
            if (null != error) System.err.println("Scenario stopped due to error. See logs for details.");
            System.err.flush();
            System.out.flush();
            return NBCLI.EXIT_ERROR;
        }
    }

    public Integer applyDirect(final String[] args) {

        // Initial logging config covers only command line parsing
        // We don't want anything to go to console here unless it is a real problem
        // as some integrations will depend on a stable and parsable program output
//        new LoggerConfig()
//                .setConsoleLevel(NBLogLevel.INFO.ERROR)
//                .setLogfileLevel(NBLogLevel.ERROR)
//                .activate();
//        logger = LogManager.getLogger("NBCLI");

        NBCLI.loggerConfig.setConsoleLevel(NBLogLevel.ERROR);
        this.sessionTime = System.currentTimeMillis();
        final NBCLIOptions globalOptions = new NBCLIOptions(args, Mode.ParseGlobalsOnly);
        this.labels = globalOptions.getLabels();
        this.sessionCode = SystemId.genSessionCode(sessionTime);
        this.sessionName = SessionNamer.format(globalOptions.getSessionName(), sessionTime).replaceAll("SESSIONCODE", sessionCode);

        NBCLI.loggerConfig
            .setSessionName(sessionName)
            .setConsoleLevel(globalOptions.getConsoleLogLevel())
            .setConsolePattern(globalOptions.getConsoleLoggingPattern())
            .setLogfileLevel(globalOptions.getScenarioLogLevel())
            .setLogfilePattern(globalOptions.getLogfileLoggingPattern())
            .setLoggerLevelOverrides(globalOptions.getLogLevelOverrides())
            .setMaxLogs(globalOptions.getLogsMax())
            .setLogsDirectory(globalOptions.getLogsDirectory())
            .setAnsiEnabled(globalOptions.isEnableAnsi())
            .setDedicatedVerificationLogger(globalOptions.isDedicatedVerificationLogger())
            .activate();
        ConfigurationFactory.setConfigurationFactory(NBCLI.loggerConfig); // THIS should be the first time log4j2 is invoked!

        NBCLI.logger = LogManager.getLogger("NBCLI"); // TODO: Detect if the logger config was already initialized (error)
        NBCLI.loggerConfig.purgeOldFiles(LogManager.getLogger("SCENARIO"));
        if (NBCLI.logger.isInfoEnabled())
            NBCLI.logger.info(() -> "Configured scenario log at " + NBCLI.loggerConfig.getLogfileLocation());
        else System.err.println("Configured scenario log at " + NBCLI.loggerConfig.getLogfileLocation());
        NBCLI.logger.debug("Scenario log started");

        // Global only processing
        if (0 == args.length) {
            System.out.println(this.loadHelpFile("commandline.md"));
            return NBCLI.EXIT_OK;
        }

        NBCLI.logger.info(() -> "Running NoSQLBench Version " + new VersionInfo().getVersion());
        NBCLI.logger.info(() -> "command-line: " + String.join(" ", args));
        NBCLI.logger.info(() -> "client-hardware: " + SystemId.getHostSummary());


        // Invoke any bundled app which matches the name of the first non-option argument, if it exists.
        // If it does not, continue with no fanfare. Let it drop through to other command resolution methods.
        if ((0 < args.length) && args[0].matches("\\w[\\w\\d-_.]+")) {
            final ServiceSelector<BundledApp> apploader = ServiceSelector.of(args[0], ServiceLoader.load(BundledApp.class));
            final BundledApp app = apploader.get().orElse(null);
            if (null != app) {
                final String[] appargs = Arrays.copyOfRange(args, 1, args.length);
                NBCLI.logger.info(() -> "invoking bundled app '" + args[0] + "' (" + app.getClass().getSimpleName() + ").");
                globalOptions.setWantsStackTraces(true);
                final int result = app.applyAsInt(appargs);
                return result;
            }
        }

        String annotatorsConfig = globalOptions.getAnnotatorsConfig();

        if (annotatorsConfig == null || annotatorsConfig.isBlank()) {
            List<Map<String, String>> annotatorsConfigs = new ArrayList<>();
            annotatorsConfigs.add(Map.of(
                "type", "log",
                "level", "info"
            ));

            Gson gson = new GsonBuilder().create();
            annotatorsConfig = gson.toJson(annotatorsConfigs);
        }

        final NBCLIOptions options = new NBCLIOptions(args, Mode.ParseAllOptions);
        NBCLI.logger = LogManager.getLogger("NBCLI");

        NBIO.addGlobalIncludes(options.wantsIncludes());

        if (options.wantsBasicHelp()) {
            System.out.println(this.loadHelpFile("basic.md"));
            return NBCLI.EXIT_OK;
        }

        if (options.isWantsVersionShort()) {
            System.out.println(new VersionInfo().getVersion());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsVersionCoords()) {
            System.out.println(new VersionInfo().getArtifactCoordinates());
            return NBCLI.EXIT_OK;
        }

        if (options.isWantsListApps()) {
            final ServiceLoader<BundledApp> loader = ServiceLoader.load(BundledApp.class);
            for (final Provider<BundledApp> provider : loader.stream().toList()) {
                final Class<? extends BundledApp> appType = provider.type();
                final String name = appType.getAnnotation(Service.class).selector();
                System.out.printf("%-40s %s%n", name, appType.getCanonicalName());
            }
            return NBCLI.EXIT_OK;
        }

        if (options.getWantsListCommands()) {
            for (CmdType value : CmdType.values()) {
                System.out.println(value.name());
            }
            return NBCLI.EXIT_OK;
        }
        if (options.wantsActivityTypes()) {
            new ActivityTypeLoader().getAllSelectors().forEach(System.out::println);
            return NBCLI.EXIT_OK;
        }

        if (options.wantsWorkloadsList()) {
            NBCLIScenarios.printWorkloads(false, options.wantsIncludes());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsScenariosList()) {
            NBCLIScenarios.printWorkloads(true, options.wantsIncludes());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsListScripts()) {
            NBCLIScripts.printScripts(true, options.wantsIncludes());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsToCatResource()) {
            final String resourceToCat = options.wantsToCatResourceNamed();
            NBCLI.logger.debug(() -> "user requests to cat " + resourceToCat);

            Optional<Content<?>> tocat = NBIO.classpath()
                .searchPrefixes("activities")
                .searchPrefixes(options.wantsIncludes())
                .pathname(resourceToCat).extensionSet(RawOpsLoader.YAML_EXTENSIONS).first();

            if (tocat.isEmpty()) tocat = NBIO.classpath()
                .searchPrefixes().searchPrefixes(options.wantsIncludes())
                .searchPrefixes(options.wantsIncludes())
                .pathname(resourceToCat).first();

            final Content<?> data = tocat.orElseThrow(
                () -> new BasicError("Unable to find " + resourceToCat +
                    " in classpath to cat out"));

            System.out.println(data.get());
            NBCLI.logger.info(() -> "Dumped internal resource '" + data.asPath() + "' to stdout");
            return NBCLI.EXIT_OK;
        }

        if (options.wantsToCopyResource()) {
            final String resourceToCopy = options.wantsToCopyResourceNamed();
            NBCLI.logger.debug(() -> "user requests to copy out " + resourceToCopy);

            Optional<Content<?>> tocopy = NBIO.classpath()
                .searchPrefixes("activities")
                .searchPrefixes(options.wantsIncludes())
                .pathname(resourceToCopy).extensionSet(RawOpsLoader.YAML_EXTENSIONS).first();

            if (tocopy.isEmpty()) tocopy = NBIO.classpath()
                .searchPrefixes().searchPrefixes(options.wantsIncludes())
                .searchPrefixes(options.wantsIncludes())
                .pathname(resourceToCopy).first();

            final Content<?> data = tocopy.orElseThrow(
                () -> new BasicError(
                    "Unable to find " + resourceToCopy +
                        " in classpath to copy out")
            );

            final Path writeTo = Path.of(data.asPath().getFileName().toString());
            if (Files.exists(writeTo)) throw new BasicError("A file named " + writeTo + " exists. Remove it first.");
            try {
                Files.writeString(writeTo, data.getCharBuffer(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new BasicError("Unable to write to " + writeTo + ": " + e.getMessage());
            }
            NBCLI.logger.info(() -> "Copied internal resource '" + data.asPath() + "' to '" + writeTo + '\'');
            return NBCLI.EXIT_OK;

        }

        if (options.wantsInputTypes()) {
            InputType.FINDER.getAllSelectors().forEach((k, v) -> System.out.println(k + " (" + v.name() + ')'));
            return NBCLI.EXIT_OK;
        }

        if (options.wantsMarkerTypes()) {
            OutputType.FINDER.getAllSelectors().forEach((k, v) -> System.out.println(k + " (" + v.name() + ')'));
            return NBCLI.EXIT_OK;
        }

        if (options.wantsToDumpCyclelog()) {
            CycleLogDumperUtility.main(options.getCycleLogExporterOptions());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsToImportCycleLog()) {
            CycleLogImporterUtility.main(options.getCyclelogImportOptions());
            return NBCLI.EXIT_OK;
        }

        if (options.wantsTopicalHelp()) {
            final Optional<String> helpDoc = MarkdownFinder.forHelpTopic(options.wantsTopicalHelpFor());
            System.out.println(helpDoc.orElseThrow(
                () -> new RuntimeException("No help could be found for " + options.wantsTopicalHelpFor())
            ));
            return NBCLI.EXIT_OK;
        }

        NBCLI.logger.debug("initializing annotators with config:'{}'", annotatorsConfig);
        Annotators.init(annotatorsConfig, options.getAnnotateLabelSpec());
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .element(this)
                .now()
                .layer(Layer.Session)
                .addDetail("cli", String.join("\n", args))
                .build()
        );

//        if ((null != reportPromPushTo) || (null != reportGraphiteTo) || (null != options.wantsReportCsvTo())) {
//            final MetricReporters reporters = MetricReporters.getInstance();
//            reporters.addRegistry("workloads", ActivityMetrics.getMetricRegistry());
//
//            if (null != reportPromPushTo)
//                reporters.addPromPush(reportPromPushTo, options.wantsMetricsPrefix(), promPushConfig);
//            if (null != reportGraphiteTo) reporters.addGraphite(reportGraphiteTo, options.wantsMetricsPrefix());
//            if (null != options.wantsReportCsvTo())
//                reporters.addCSVReporter(options.wantsReportCsvTo(), options.wantsMetricsPrefix());
//            if (options.wantsLoggedMetrics()) {
//                reporters.addLogger();
//            }
//            reporters.start(10, options.getReportInterval());
//        }
//
//        if (options.getConsoleLogLevel().isGreaterOrEqualTo(NBLogLevel.WARN)) {
//            options.setWantsStackTraces(true);
//            NBCLI.logger.debug(() -> "enabling stack traces since log level is " + options.getConsoleLogLevel());
//        }

        // client machine metrics; TODO: modify pollInterval

        // intentionally not shown for warn-only
        NBCLI.logger.info(() -> "console logging level is " + options.getConsoleLogLevel());

        Map<String, String> props = Map.of(
            "summary", options.getReportSummaryTo(),
            "logsdir", options.getLogsDirectory().toString(),
            "progress", options.getProgressSpec(),
            "prompush_cache", "prompush_cache.txt",
            "heartbeat", String.valueOf(options.wantsHeartbeatIntervalMs())
        );
        /**
         * At this point, the command stream from the CLI should be handed into the session, and the session should
         * marshal and transform it for any scenario invocations directly.
         */

        try (
            NBSession session = new NBSession(
                new NBBaseComponent(null,
                    options.getLabelMap()
                        .andDefault("jobname", "nosqlbench")
                        .andDefault("instance", "default")
                ),
                sessionName,
                props
            )) {

            options.wantsReportCsvTo().ifPresent(cfg -> {
                MetricInstanceFilter filter = new MetricInstanceFilter();
                filter.addPattern(cfg.pattern);
                new CsvReporter(session, Path.of(cfg.file), cfg.millis, filter);
            });

            options.wantsReportPromPushTo().ifPresent(cfg -> {
                String[] words = cfg.split(",");
                String uri;
                long intervalMs = 10_000L;

                switch (words.length) {
                    case 2:
                        intervalMs = Unit.msFor(words[1]).orElseThrow(() -> new RuntimeException("can't parse '" + words[1] + "!"));
                    case 1:
                        uri = words[0];
                        break;
                    default:
                        throw new RuntimeException("Unable to parse '" + cfg + "', must be in <URI> or <URI>,ms form");
                }
                session.create().pushReporter(uri, intervalMs, NBLabels.forKV());
            });
            for (final NBCLIOptions.LoggerConfigData histoLogger : options.getHistoLoggerConfigs()) {
                session.create().histoLogger(sessionName, histoLogger.pattern, histoLogger.file, histoLogger.millis);
            }
            for (final NBCLIOptions.LoggerConfigData statsLogger : options.getStatsLoggerConfigs()) {
                session.create().histoStatsLogger(sessionName, statsLogger.pattern, statsLogger.file, statsLogger.millis);
            }

            ExecutionResult sessionResult = session.apply(options.getCommands());
            logger.info(sessionResult);
            if (sessionResult.getException() instanceof RuntimeException rte) {
                throw rte;
            } else if (sessionResult.getException() instanceof Throwable t) {
                throw new RuntimeException(t);
            }

            return sessionResult.getStatus().code;
        }
//        sessionResult.printSummary(System.out);

    }


    private String loadHelpFile(final String filename) {
        final ClassLoader cl = this.getClass().getClassLoader();
        final InputStream resourceAsStream = cl.getResourceAsStream(filename);
        if (null == resourceAsStream) throw new RuntimeException("Unable to find " + filename + " in classpath.");
        String basicHelp;
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            basicHelp = buffer.lines().collect(Collectors.joining("\n"));
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to buffer " + filename + ": " + t);
        }
        basicHelp = basicHelp.replaceAll("PROG", this.commandName);
        return basicHelp;
    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }

}

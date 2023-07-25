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

import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.logging.NBLogLevel;
import io.nosqlbench.api.metadata.SessionNamer;
import io.nosqlbench.api.metadata.SystemId;
import io.nosqlbench.api.apps.BundledApp;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogDumperUtility;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogImporterUtility;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.engine.cli.NBCLIOptions.LoggerConfigData;
import io.nosqlbench.engine.cli.NBCLIOptions.Mode;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityTypeLoader;
import io.nosqlbench.engine.core.lifecycle.process.ShutdownManager;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosResults;
import io.nosqlbench.engine.core.logging.LoggerConfig;
import io.nosqlbench.engine.core.metadata.MarkdownFinder;
import io.nosqlbench.engine.core.metrics.MetricReporters;
import io.nosqlbench.engine.core.lifecycle.scenario.script.MetricsMapper;
import io.nosqlbench.engine.core.lifecycle.scenario.Scenario;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScriptParams;
import io.nosqlbench.engine.docker.DockerMetricsManager;
import io.nosqlbench.nb.annotations.Maturity;
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
    private static final LoggerConfig loggerConfig;
    private static final int EXIT_OK = 0;
    private static final int EXIT_WARNING = 1;
    private static final int EXIT_ERROR = 2;

    static {
        loggerConfig = new LoggerConfig();
        ConfigurationFactory.setConfigurationFactory(NBCLI.loggerConfig);
    }

    private final String commandName;

    private NBLabels labels;
    private String sessionName;

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

        final NBCLIOptions globalOptions = new NBCLIOptions(args, Mode.ParseGlobalsOnly);
        this.labels=NBLabels.forKV("command",commandName).and(globalOptions.getLabelMap());
        this.sessionName = SessionNamer.format(globalOptions.getSessionName());

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
        ConfigurationFactory.setConfigurationFactory(NBCLI.loggerConfig);

        NBCLI.logger = LogManager.getLogger("NBCLI");
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
        NBCLI.logger.info(() -> "command-line: " + Arrays.stream(args).collect(Collectors.joining(" ")));
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


        final boolean dockerMetrics = globalOptions.wantsDockerMetrics();
        final String dockerMetricsAt = globalOptions.wantsDockerMetricsAt();
        String reportGraphiteTo = globalOptions.wantsReportGraphiteTo();
        String annotatorsConfig = globalOptions.getAnnotatorsConfig();
        final String reportPromPushTo = globalOptions.wantsReportPromPushTo();



        final int mOpts = (dockerMetrics ? 1 : 0)
            + ((null != dockerMetricsAt) ? 1 : 0)
            + ((null != reportGraphiteTo) ? 1 : 0);

        if ((1 < mOpts) && ((null == reportGraphiteTo) || (null == annotatorsConfig)))
            throw new BasicError("You have multiple conflicting options which attempt to set\n" +
                " the destination for metrics and annotations. Please select only one of\n" +
                " --docker-metrics, --docker-metrics-at <addr>, or other options like \n" +
                " --report-graphite-to <addr> and --annotators <config>\n" +
                " For more details, see run 'nb help docker-metrics'");

        String graphiteMetricsAddress = null;

        if (dockerMetrics) {
            // Setup docker stack for local docker metrics
            NBCLI.logger.info("Docker metrics is enabled. Docker must be installed for this to work");
            final DockerMetricsManager dmh = new DockerMetricsManager();
            final Map<String, String> dashboardOptions = Map.of(
                DockerMetricsManager.GRAFANA_TAG, globalOptions.getDockerGrafanaTag(),
                DockerMetricsManager.PROM_TAG, globalOptions.getDockerPromTag(),
                DockerMetricsManager.TSDB_RETENTION, String.valueOf(globalOptions.getDockerPromRetentionDays()),
                DockerMetricsManager.GRAPHITE_SAMPLE_EXPIRY, "10m",
                DockerMetricsManager.GRAPHITE_CACHE_SIZE, "5000",
                DockerMetricsManager.GRAPHITE_LOG_LEVEL, globalOptions.getGraphiteLogLevel(),
                DockerMetricsManager.GRAPHITE_LOG_FORMAT, "logfmt"

            );
            dmh.startMetrics(dashboardOptions);
            final String warn = "Docker Containers are started, for grafana and prometheus, hit" +
                " these urls in your browser: http://<host>:3000 and http://<host>:9090";
            NBCLI.logger.warn(warn);
            graphiteMetricsAddress = "localhost";
        } else if (null != dockerMetricsAt) graphiteMetricsAddress = dockerMetricsAt;

        if (null != graphiteMetricsAddress) {
            reportGraphiteTo = graphiteMetricsAddress + ":9109";
            annotatorsConfig = "[{type:'log',level:'info'},{type:'grafana',baseurl:'http://" + graphiteMetricsAddress + ":3000" +
                "/'," +
                "tags:'appname:nosqlbench',timeoutms:5000,onerror:'warn'}]";
        } else annotatorsConfig = "[{type:'log',level:'info'}]";

        final NBCLIOptions options = new NBCLIOptions(args);
        NBCLI.logger = LogManager.getLogger("NBCLI");

        NBIO.addGlobalIncludes(options.wantsIncludes());

        ActivityMetrics.setHdrDigits(options.getHdrDigits());

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
            NBCLICommandParser.RESERVED_WORDS.forEach(System.out::println);
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

        if (null != options.wantsMetricsForActivity()) {
            final String metricsHelp = this.getMetricsHelpFor(options.wantsMetricsForActivity());
            System.out.println("Available metric names for activity:" + options.wantsMetricsForActivity() + ':');
            System.out.println(metricsHelp);
            return NBCLI.EXIT_OK;
        }

        NBCLI.logger.debug("initializing annotators with config:'{}'", annotatorsConfig);
        Annotators.init(annotatorsConfig);
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .session(sessionName)
                .now()
                .layer(Layer.CLI)
                .detail("cli", String.join("\n", args))
                .build()
        );

        if ((null != reportPromPushTo) || (null != reportGraphiteTo) || (null != options.wantsReportCsvTo())) {
            final MetricReporters reporters = MetricReporters.getInstance();
            reporters.addRegistry("workloads", ActivityMetrics.getMetricRegistry());

            if (null != reportPromPushTo) reporters.addPromPush(reportPromPushTo, options.wantsMetricsPrefix());
            if (null != reportGraphiteTo) reporters.addGraphite(reportGraphiteTo, options.wantsMetricsPrefix());
            if (null != options.wantsReportCsvTo())
                reporters.addCSVReporter(options.wantsReportCsvTo(), options.wantsMetricsPrefix());
            reporters.addLogger();
            reporters.start(10, options.getReportInterval());
        }

        if (options.wantsEnableChart()) {
            NBCLI.logger.info("Charting enabled");
            if (0 == options.getHistoLoggerConfigs().size()) {
                NBCLI.logger.info("Adding default histologger configs");
                final String pattern = ".*";
                final String file = options.getChartHdrFileName();
                final String interval = "1s";
                options.setHistoLoggerConfigs(pattern, file, interval);
            }
        }

        for (
            final LoggerConfigData histoLogger : options.getHistoLoggerConfigs())
            ActivityMetrics.addHistoLogger(sessionName, histoLogger.pattern, histoLogger.file, histoLogger.interval);
        for (
            final LoggerConfigData statsLogger : options.getStatsLoggerConfigs())
            ActivityMetrics.addStatsLogger(sessionName, statsLogger.pattern, statsLogger.file, statsLogger.interval);
        for (
            final LoggerConfigData classicConfigs : options.getClassicHistoConfigs())
            ActivityMetrics.addClassicHistos(sessionName, classicConfigs.pattern, classicConfigs.file, classicConfigs.interval);

        // intentionally not shown for warn-only
        NBCLI.logger.info(() -> "console logging level is " + options.getConsoleLogLevel());

        final ScenariosExecutor scenariosExecutor = new ScenariosExecutor("executor-" + sessionName, 1);
        if (options.getConsoleLogLevel().isGreaterOrEqualTo(NBLogLevel.WARN)) {
            options.setWantsStackTraces(true);
            NBCLI.logger.debug(() -> "enabling stack traces since log level is " + options.getConsoleLogLevel());
        }

        final Scenario scenario = new Scenario(
            sessionName,
            options.getScriptFile(),
            options.getScriptingEngine(),
            options.getProgressSpec(),
            options.wantsStackTraces(),
            options.wantsCompileScript(),
            options.getReportSummaryTo(),
            String.join("\n", args),
            options.getLogsDirectory(),
            Maturity.Unspecified,
            this);

        final ScriptBuffer buffer = new BasicScriptBuffer()
            .add(options.getCommands()
                .toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        if (options.wantsShowScript()) {
            System.out.println("// Rendered Script");
            System.out.println(scriptData);
            return NBCLI.EXIT_OK;
        }

        if (options.wantsEnableChart()) {
            NBCLI.logger.info("Charting enabled");
            scenario.enableCharting();
        } else NBCLI.logger.info("Charting disabled");


        // Execute Scenario!
        if (0 == options.getCommands().size()) {
            NBCLI.logger.info("No commands provided. Exiting before scenario.");
            return NBCLI.EXIT_OK;
        }

        scenario.addScriptText(scriptData);
        final ScriptParams scriptParams = new ScriptParams();
        scriptParams.putAll(buffer.getCombinedParams());
        scenario.addScenarioScriptParams(scriptParams);

        scenariosExecutor.execute(scenario);
        final ScenariosResults scenariosResults = scenariosExecutor.awaitAllResults();
        NBCLI.logger.debug(() -> "Total of " + scenariosResults.getSize() + " result object returned from ScenariosExecutor");

        ActivityMetrics.closeMetrics(options.wantsEnableChart());
        scenariosResults.reportToLog();
        ShutdownManager.shutdown();

        NBCLI.logger.info(scenariosResults.getExecutionSummary());

        if (scenariosResults.hasError()) {
            final Exception exception = scenariosResults.getOne().getException();
            NBCLI.logger.warn(scenariosResults.getExecutionSummary());
            NBCLIErrorHandler.handle(exception, options.wantsStackTraces());
            System.err.println(exception.getMessage()); // TODO: make this consistent with ConsoleLogging sequencing
            return NBCLI.EXIT_ERROR;
        }
        NBCLI.logger.info(scenariosResults.getExecutionSummary());
        return NBCLI.EXIT_OK;

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

    private String getMetricsHelpFor(final String activityType) {
        final String metrics = MetricsMapper.metricsDetail(activityType);
        return metrics;
    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }
}

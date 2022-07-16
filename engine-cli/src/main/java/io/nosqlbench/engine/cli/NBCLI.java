/*
 * Copyright (c) 2022 nosqlbench
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

import io.nosqlbench.api.docsapi.docexporter.BundledMarkdownExporter;
import io.nosqlbench.docsys.core.NBWebServerApp;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogDumperUtility;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogImporterUtility;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsLoader;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.*;
import io.nosqlbench.engine.core.logging.LoggerConfig;
import io.nosqlbench.engine.core.metadata.MarkdownDocInfo;
import io.nosqlbench.engine.core.metrics.MetricReporters;
import io.nosqlbench.engine.core.script.MetricsMapper;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import io.nosqlbench.engine.core.script.ScriptParams;
import io.nosqlbench.engine.docker.DockerMetricsManager;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.logging.NBLogLevel;
import io.nosqlbench.api.markdown.exporter.MarkdownExporter;
import io.nosqlbench.api.metadata.SessionNamer;
import io.nosqlbench.api.metadata.SystemId;
import io.nosqlbench.virtdata.userlibs.apps.VirtDataMainApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBCLI implements Function<String[], Integer> {

    private static Logger logger;
    private static LoggerConfig loggerConfig;
    private static int EXIT_OK = 0;
    private static int EXIT_WARNING = 1;
    private static int EXIT_ERROR = 2;

    static {
        loggerConfig = new LoggerConfig();
        LoggerConfig.setConfigurationFactory(loggerConfig);
    }

    private final String commandName;

    public NBCLI(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Only call System.exit with the body of main. This is so that other scenario
     * invocations are handled functionally by {@link #apply(String[])}, which allows
     * for scenario encapsulation and concurrent testing.
     * @param args Command Line Args
     */
    public static void main(String[] args) {
        try {
            NBCLI cli = new NBCLI("nb");
            int statusCode = cli.apply(args);
            System.exit(statusCode);
        } catch (Exception e) {

        }
    }
    /**
     *         return null;
     *     }
     *
     *     public static void main(String[] args) {
     * @param strings
     * @return
     */
    @Override
    public Integer apply(String[] args) {
        try {
            NBCLI cli = new NBCLI("nb");
            int result = cli.applyDirect(args);
            return result;
        } catch (Exception e) {
            boolean showStackTraces = false;
            for (String arg : args) {

                if (arg.toLowerCase(Locale.ROOT).startsWith("-v") || (arg.toLowerCase(Locale.ROOT).equals("--show-stacktraces"))) {
                    showStackTraces = true;
                }
            }

            String error = ScenarioErrorHandler.handle(e, showStackTraces);
            // Commented for now, as the above handler should do everything needed.
            if (error != null) {
                System.err.println("Scenario stopped due to error. See logs for details.");
            }
            System.err.flush();
            System.out.flush();
            return EXIT_ERROR;
        }
    }

    public Integer applyDirect(String[] args) {

        // Initial logging config covers only command line parsing
        // We don't want anything to go to console here unless it is a real problem
        // as some integrations will depend on a stable and parsable program output
//        new LoggerConfig()
//                .setConsoleLevel(NBLogLevel.INFO.ERROR)
//                .setLogfileLevel(NBLogLevel.ERROR)
//                .activate();
//        logger = LogManager.getLogger("NBCLI");

        loggerConfig.setConsoleLevel(NBLogLevel.ERROR);

        NBCLIOptions globalOptions = new NBCLIOptions(args, NBCLIOptions.Mode.ParseGlobalsOnly);
        String sessionName = SessionNamer.format(globalOptions.getSessionName());

        loggerConfig
            .setSessionName(sessionName)
            .setConsoleLevel(globalOptions.getConsoleLogLevel())
            .setConsolePattern(globalOptions.getConsoleLoggingPattern())
            .setLogfileLevel(globalOptions.getScenarioLogLevel())
            .setLogfilePattern(globalOptions.getLogfileLoggingPattern())
            .getLoggerLevelOverrides(globalOptions.getLogLevelOverrides())
            .setMaxLogs(globalOptions.getLogsMax())
            .setLogsDirectory(globalOptions.getLogsDirectory())
            .setAnsiEnabled(globalOptions.isEnableAnsi())
            .activate();
        ConfigurationFactory.setConfigurationFactory(loggerConfig);

        logger = LogManager.getLogger("NBCLI");
        loggerConfig.purgeOldFiles(LogManager.getLogger("SCENARIO"));
        logger.info("Configured scenario log at " + loggerConfig.getLogfileLocation());
        logger.debug("Scenario log started");

        // Global only processing
        if (args.length == 0) {
            System.out.println(loadHelpFile("commandline.md"));
            return EXIT_OK;
        }

        logger.info("Running NoSQLBench Version " + new VersionInfo().getVersion());
        logger.info("command-line: " + Arrays.stream(args).collect(Collectors.joining(" ")));
        logger.info("client-hardware: " + SystemId.getHostSummary());

        boolean dockerMetrics = globalOptions.wantsDockerMetrics();
        String dockerMetricsAt = globalOptions.wantsDockerMetricsAt();
        String reportGraphiteTo = globalOptions.wantsReportGraphiteTo();
        String annotatorsConfig = globalOptions.getAnnotatorsConfig();

        int mOpts = (dockerMetrics ? 1 : 0) + (dockerMetricsAt != null ? 1 : 0) + (reportGraphiteTo != null ? 1 : 0);
        if (mOpts > 1 && (reportGraphiteTo == null || annotatorsConfig == null)) {
            throw new BasicError("You have multiple conflicting options which attempt to set\n" +
                " the destination for metrics and annotations. Please select only one of\n" +
                " --docker-metrics, --docker-metrics-at <addr>, or other options like \n" +
                " --report-graphite-to <addr> and --annotators <config>\n" +
                " For more details, see run 'nb help docker-metrics'");
        }

        String metricsAddr = null;

        if (dockerMetrics) {
            // Setup docker stack for local docker metrics
            logger.info("Docker metrics is enabled. Docker must be installed for this to work");
            DockerMetricsManager dmh = new DockerMetricsManager();
            Map<String, String> dashboardOptions = Map.of(
                DockerMetricsManager.GRAFANA_TAG, globalOptions.getDockerGrafanaTag(),
                DockerMetricsManager.PROM_TAG, globalOptions.getDockerPromTag(),
                DockerMetricsManager.TSDB_RETENTION, String.valueOf(globalOptions.getDockerPromRetentionDays()),
                DockerMetricsManager.GRAPHITE_SAMPLE_EXPIRY,"10m",
                DockerMetricsManager.GRAPHITE_CACHE_SIZE,"5000",
                DockerMetricsManager.GRAPHITE_LOG_LEVEL,globalOptions.getGraphiteLogLevel(),
                DockerMetricsManager.GRAPHITE_LOG_FORMAT,"logfmt"

            );
            dmh.startMetrics(dashboardOptions);
            String warn = "Docker Containers are started, for grafana and prometheus, hit" +
                " these urls in your browser: http://<host>:3000 and http://<host>:9090";
            logger.warn(warn);
            metricsAddr = "localhost";
        } else if (dockerMetricsAt != null) {
            metricsAddr = dockerMetricsAt;
        }

        if (metricsAddr != null) {
            reportGraphiteTo = metricsAddr + ":9109";
            annotatorsConfig = "[{type:'log',level:'info'},{type:'grafana',baseurl:'http://" + metricsAddr + ":3000" +
                "/'," +
                "tags:'appname:nosqlbench',timeoutms:5000,onerror:'warn'}]";
        } else {
            annotatorsConfig = "[{type:'log',level:'info'}]";
        }

        if (args.length > 0 && args[0].toLowerCase().equals("cqlgen")) {
            String exporterImpl = "io.nosqlbench.converters.cql.exporters.CGWorkloadExporter";
            String[] exporterArgs = Arrays.copyOfRange(args, 1, args.length);
            try {
                Class<?> genclass = Class.forName(exporterImpl);
                Method main = genclass.getMethod("main", new String[0].getClass());
                Object result = main.invoke(null, new Object[]{exporterArgs});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("cql workload exporter implementation " + exporterImpl + " was not found in this runtime.");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error in app: " + e.toString());
                e.printStackTrace();
                throw new RuntimeException("error while invoking " + exporterImpl + ": " + e.toString(),e);
            }
            return EXIT_OK;
        }
        if (args.length > 0 && args[0].toLowerCase().equals("export-docs")) {
            BundledMarkdownExporter.main(Arrays.copyOfRange(args,1,args.length));
            return EXIT_OK;
        }
        if (args.length > 0 && args[0].toLowerCase().equals("virtdata")) {
            VirtDataMainApp.main(Arrays.copyOfRange(args, 1, args.length));
            return EXIT_OK;
        }
        if (args.length > 0 && args[0].toLowerCase().matches("docserver|appserver")) {
            NBWebServerApp.main(Arrays.copyOfRange(args, 1, args.length));
            return EXIT_OK;
        }
        if (args.length > 0 && args[0].toLowerCase().equals(MarkdownExporter.APP_NAME)
        ) {
            MarkdownExporter.main(Arrays.copyOfRange(args, 1, args.length));
            return EXIT_OK;
        }

        NBCLIOptions options = new NBCLIOptions(args);
        logger = LogManager.getLogger("NBCLI");

        NBIO.addGlobalIncludes(options.wantsIncludes());

        ActivityMetrics.setHdrDigits(options.getHdrDigits());

        if (options.wantsBasicHelp()) {
            System.out.println(loadHelpFile("basic.md"));
            return EXIT_OK;
        }

        if (options.isWantsVersionShort()) {
            System.out.println(new VersionInfo().getVersion());
            return EXIT_OK;
        }

        if (options.wantsVersionCoords()) {
            System.out.println(new VersionInfo().getArtifactCoordinates());
            return EXIT_OK;
        }

        if (options.wantsActivityTypes()) {
            new ActivityTypeLoader().getAllSelectors().forEach(System.out::println);
            return EXIT_OK;
        }

        if (options.wantsWorkloadsList()) {
            NBCLIScenarios.printWorkloads(false, options.wantsIncludes());
            return EXIT_OK;
        }

        if (options.wantsScenariosList()) {
            NBCLIScenarios.printWorkloads(true, options.wantsIncludes());
            return EXIT_OK;
        }

        if (options.wantsScriptList()) {
            NBCLIScripts.printScripts(true, options.wantsIncludes());
            return EXIT_OK;
        }

        if (options.wantsToCopyResource()) {
            String resourceToCopy = options.wantsToCopyResourceNamed();
            logger.debug("user requests to copy out " + resourceToCopy);

            Optional<Content<?>> tocopy = NBIO.classpath()
                .prefix("activities")
                .prefix(options.wantsIncludes())
                .name(resourceToCopy).extension(RawStmtsLoader.YAML_EXTENSIONS).first();

            if (tocopy.isEmpty()) {

                tocopy = NBIO.classpath()
                    .prefix().prefix(options.wantsIncludes())
                    .prefix(options.wantsIncludes())
                    .name(resourceToCopy).first();
            }

            Content<?> data = tocopy.orElseThrow(
                () -> new BasicError(
                    "Unable to find " + resourceToCopy +
                        " in classpath to copy out")
            );

            Path writeTo = Path.of(data.asPath().getFileName().toString());
            if (Files.exists(writeTo)) {
                throw new BasicError("A file named " + writeTo.toString() + " exists. Remove it first.");
            }
            try {
                Files.writeString(writeTo, data.getCharBuffer(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new BasicError("Unable to write to " + writeTo.toString() + ": " + e.getMessage());
            }
            logger.info("Copied internal resource '" + data.asPath() + "' to '" + writeTo.toString() + "'");
            return EXIT_OK;

        }

        if (options.wantsInputTypes()) {
            InputType.FINDER.getAllSelectors().forEach((k,v) -> System.out.println(k + " (" + v.name() + ")"));
            return EXIT_OK;
        }

        if (options.wantsMarkerTypes()) {
            OutputType.FINDER.getAllSelectors().forEach((k,v) -> System.out.println(k + " (" + v.name() + ")"));
            return EXIT_OK;
        }

        if (options.wantsToDumpCyclelog()) {
            CycleLogDumperUtility.main(options.getCycleLogExporterOptions());
            return EXIT_OK;
        }

        if (options.wantsToImportCycleLog()) {
            CycleLogImporterUtility.main(options.getCyclelogImportOptions());
            return EXIT_OK;
        }

        if (options.wantsTopicalHelp()) {
            Optional<String> helpDoc = MarkdownDocInfo.forHelpTopic(options.wantsTopicalHelpFor());
            System.out.println(helpDoc.orElseThrow(
                () -> new RuntimeException("No help could be found for " + options.wantsTopicalHelpFor())
            ));
            return EXIT_OK;
        }

        if (options.wantsMetricsForActivity() != null) {
            String metricsHelp = getMetricsHelpFor(options.wantsMetricsForActivity());
            System.out.println("Available metric names for activity:" + options.wantsMetricsForActivity() + ":");
            System.out.println(metricsHelp);
            return EXIT_OK;
        }

        logger.debug("initializing annotators with config:'" + annotatorsConfig + "'");
        Annotators.init(annotatorsConfig);
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .session(sessionName)
                .now()
                .layer(Layer.CLI)
                .detail("cli", String.join("\n", args))
                .build()
        );

        if (reportGraphiteTo != null || options.wantsReportCsvTo() != null) {
            MetricReporters reporters = MetricReporters.getInstance();
            reporters.addRegistry("workloads", ActivityMetrics.getMetricRegistry());

            if (reportGraphiteTo != null) {
                reporters.addGraphite(reportGraphiteTo, options.wantsMetricsPrefix());
            }
            if (options.wantsReportCsvTo() != null) {
                reporters.addCSVReporter(options.wantsReportCsvTo(), options.wantsMetricsPrefix());
            }
            reporters.start(10, options.getReportInterval());
        }

        if (options.wantsEnableChart()) {
            logger.info("Charting enabled");
            if (options.getHistoLoggerConfigs().size() == 0) {
                logger.info("Adding default histologger configs");
                String pattern = ".*";
                String file = options.getChartHdrFileName();
                String interval = "1s";
                options.setHistoLoggerConfigs(pattern, file, interval);
            }
        }

        for (
            NBCLIOptions.LoggerConfigData histoLogger : options.getHistoLoggerConfigs()) {
            ActivityMetrics.addHistoLogger(sessionName, histoLogger.pattern, histoLogger.file, histoLogger.interval);
        }
        for (
            NBCLIOptions.LoggerConfigData statsLogger : options.getStatsLoggerConfigs()) {
            ActivityMetrics.addStatsLogger(sessionName, statsLogger.pattern, statsLogger.file, statsLogger.interval);
        }
        for (
            NBCLIOptions.LoggerConfigData classicConfigs : options.getClassicHistoConfigs()) {
            ActivityMetrics.addClassicHistos(sessionName, classicConfigs.pattern, classicConfigs.file, classicConfigs.interval);
        }

        // intentionally not shown for warn-only
        logger.info("console logging level is " + options.getConsoleLogLevel());

        ScenariosExecutor executor = new ScenariosExecutor("executor-" + sessionName, 1);
        if (options.getConsoleLogLevel().isGreaterOrEqualTo(NBLogLevel.WARN)) {
            options.setWantsStackTraces(true);
            logger.debug("enabling stack traces since log level is " + options.getConsoleLogLevel());
        }

        Scenario scenario = new Scenario(
            sessionName,
            options.getScriptFile(),
            options.getScriptingEngine(),
            options.getProgressSpec(),
            options.wantsGraaljsCompatMode(),
            options.wantsStackTraces(),
            options.wantsCompileScript(),
            options.getReportSummaryTo(),
            String.join("\n", args),
            options.getLogsDirectory(),
            Maturity.Unspecified);

        ScriptBuffer buffer = new BasicScriptBuffer()
            .add(options.getCommands()
                .toArray(new Cmd[0]));
        String scriptData = buffer.getParsedScript();

        if (options.wantsShowScript()) {
            System.out.println("// Rendered Script");
            System.out.println(scriptData);
            return EXIT_OK;
        }

        if (options.wantsEnableChart()) {
            logger.info("Charting enabled");
            scenario.enableCharting();
        } else {
            logger.info("Charting disabled");
        }


        // Execute Scenario!
        if (options.getCommands().size() == 0) {
            logger.info("No commands provided. Exiting before scenario.");
            return EXIT_OK;
        }

        scenario.addScriptText(scriptData);
        ScriptParams scriptParams = new ScriptParams();
        scriptParams.putAll(buffer.getCombinedParams());
        scenario.addScenarioScriptParams(scriptParams);

        executor.execute(scenario);

        while (true) {
            Optional<ScenarioResult> pendingResult = executor.getPendingResult(scenario.getScenarioName());
            if (pendingResult.isEmpty()) {
                LockSupport.parkNanos(100000000L);
            } else {
                break;
            }
        }

        ScenariosResults scenariosResults = executor.awaitAllResults();

        ActivityMetrics.closeMetrics(options.wantsEnableChart());
        //scenariosResults.reportToLog();
        ShutdownManager.shutdown();

//        logger.info(scenariosResults.getExecutionSummary());

        if (scenariosResults.hasError()) {
            Exception exception = scenariosResults.getOne().getException().get();
//            logger.warn(scenariosResults.getExecutionSummary());
            ScenarioErrorHandler.handle(exception, options.wantsStackTraces());
            System.err.println(exception.getMessage()); // TODO: make this consistent with ConsoleLogging sequencing
            return EXIT_ERROR;
        } else {
            logger.info(scenariosResults.getExecutionSummary());
            return EXIT_OK;
        }

    }

    private String loadHelpFile(String filename) {
        ClassLoader cl = getClass().getClassLoader();
        InputStream resourceAsStream = cl.getResourceAsStream(filename);
        if (resourceAsStream == null) {
            throw new RuntimeException("Unable to find " + filename + " in classpath.");
        }
        String basicHelp;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            basicHelp = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to buffer " + filename + ": " + t);
        }
        basicHelp = basicHelp.replaceAll("PROG", commandName);
        return basicHelp;

    }

    private String getMetricsHelpFor(String activityType) {
        String metrics = MetricsMapper.metricsDetail(activityType);
        return metrics;
    }

}

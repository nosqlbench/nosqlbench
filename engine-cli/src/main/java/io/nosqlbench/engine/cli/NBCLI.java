package io.nosqlbench.engine.cli;

import ch.qos.logback.classic.Level;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogDumperUtility;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogImporterUtility;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.core.*;
import io.nosqlbench.engine.core.script.ScriptParams;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.engine.docker.DockerMetricsManager;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.metrics.MetricReporters;
import io.nosqlbench.engine.core.script.MetricsMapper;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import io.nosqlbench.nb.api.markdown.exporter.MarkdownExporter;
import io.nosqlbench.virtdata.userlibs.apps.VirtDataMainApp;
import io.nosqlbench.docsys.core.DocServerApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

public class NBCLI {

    private static final Logger logger = LoggerFactory.getLogger(NBCLI.class);
    private static final String CHART_HDR_LOG_NAME = "hdrdata-for-chart.log";

    private String commandName;

    public NBCLI(String commandName) {
        this.commandName = commandName;
    }

    public static void main(String[] args) {
        try {
            NBCLI cli = new NBCLI("eb");
            cli.run(args);
        } catch (Exception e) {
            if (e instanceof BasicError) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.flush();
                logger.error("ERROR: " + e.getMessage());
                System.exit(2);
            } else {
                throw e;
            }
        }
    }

    public void run(String[] args) {
        if (args.length > 0 && args[0].toLowerCase().equals("virtdata")) {
            VirtDataMainApp.main(Arrays.copyOfRange(args, 1, args.length));
            System.exit(0);
        }
        if (args.length > 0 && args[0].toLowerCase().equals("docserver")) {
            DocServerApp.main(Arrays.copyOfRange(args, 1, args.length));
            System.exit(0);
        }
        if (args.length>0 && args[0].toLowerCase().equals(MarkdownExporter.APP_NAME)) {
            MarkdownExporter.main(Arrays.copyOfRange(args,1,args.length));
            System.exit(0);
        }

        NBCLIOptions options = new NBCLIOptions(args);
        ConsoleLogging.enableConsoleLogging(options.wantsConsoleLogLevel(), options.getConsoleLoggingPattern());

        if (options.wantsBasicHelp()) {
            System.out.println(loadHelpFile("basic.md"));
            System.exit(0);
        }

        if (options.isWantsVersionShort()) {
            System.out.println(new VersionInfo().getVersion());
            System.exit(0);
        }

        if (options.wantsVersionCoords()) {
            System.out.println(new VersionInfo().getArtifactCoordinates());
            System.exit(0);
        }

        if (options.wantsActivityTypes()) {
            ActivityType.FINDER.getAll().stream().map(ActivityType::getName).forEach(System.out::println);
            System.exit(0);
        }

        if (options.wantsWorkloadsList()) {
            NBCLIScenarios.printWorkloads(false, options.wantsIncludes());
            System.exit(0);
        }

        if (options.wantsScenariosList()) {
            NBCLIScenarios.printWorkloads(true, options.wantsIncludes());
            System.exit(0);
        }

        if (options.wantsToCopyResource()) {
            String resourceToCopy = options.wantsToCopyResourceNamed();
            logger.debug("user requests to copy out " + resourceToCopy);

            Optional<Content<?>> tocopy = NBIO.classpath()
                .prefix("activities")
                .prefix(options.wantsIncludes())
                .name(resourceToCopy).extension("yaml").first();

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
            System.exit(0);

        }

        if (options.wantsInputTypes()) {
            InputType.FINDER.getAll().stream().map(InputType::getName).forEach(System.out::println);
            System.exit(0);
        }

        if (options.wantsMarkerTypes()) {
            OutputType.FINDER.getAll().stream().map(OutputType::getName).forEach(System.out::println);
            System.exit(0);
        }

        if (options.wantsToDumpCyclelog()) {
            CycleLogDumperUtility.main(options.getCycleLogExporterOptions());
            System.exit(0);
        }

        if (options.wantsToImportCycleLog()) {
            CycleLogImporterUtility.main(options.getCyclelogImportOptions());
            System.exit(0);
        }

        if (options.wantsTopicalHelp()) {
            Optional<String> helpDoc = MarkdownDocInfo.forHelpTopic(options.wantsTopicalHelpFor());
            System.out.println(helpDoc.orElseThrow(
                () -> new RuntimeException("No help could be found for " + options.wantsTopicalHelpFor())
            ));
            System.exit(0);
        }

        if (options.wantsMetricsForActivity() != null) {
            String metricsHelp = getMetricsHelpFor(options.wantsMetricsForActivity());
            System.out.println("Available metric names for activity:" + options.wantsMetricsForActivity() + ":");
            System.out.println(metricsHelp);
            System.exit(0);
        }

        String reportGraphiteTo = options.wantsReportGraphiteTo();
        if (options.wantsDockerMetrics()) {
            logger.info("Docker metrics is enabled. Docker must be installed for this to work");
            DockerMetricsManager dmh = new DockerMetricsManager();
            dmh.startMetrics();
            String info = "Docker Containers are started, for grafana and prometheus, hit" +
                "these urls in your browser: http://<host>:3000 and http://<host>:9090" +
                "the default grafana creds are admin/admin";
            logger.info(info);
            System.out.println(info);
            if (reportGraphiteTo != null) {
                logger.warn(String.format("Docker metrics are enabled (--docker-metrics)" +
                        " but graphite reporting (--report-graphite-to) is set to %s \n" +
                        "usually only one of the two is configured.",
                    reportGraphiteTo));
            } else {
                //TODO: is this right?
                logger.info("Setting graphite reporting to localhost");
                reportGraphiteTo = "localhost:9109";
            }
        }

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

        String sessionName = new SessionNamer().format(options.getSessionName());

        if (options.wantsEnableChart()) {
            logger.info("Charting enabled");
            if (options.getHistoLoggerConfigs().size() == 0) {
                logger.info("Adding default histologger configs");
                String pattern = ".*";
                String file = CHART_HDR_LOG_NAME;
                String interval = "1s";
                options.setHistoLoggerConfigs(pattern, file, interval);
            }
        }

        for (NBCLIOptions.LoggerConfig histoLogger : options.getHistoLoggerConfigs()) {
            ActivityMetrics.addHistoLogger(sessionName, histoLogger.pattern, histoLogger.file, histoLogger.interval);
        }
        for (NBCLIOptions.LoggerConfig statsLogger : options.getStatsLoggerConfigs()) {
            ActivityMetrics.addStatsLogger(sessionName, statsLogger.pattern, statsLogger.file, statsLogger.interval);
        }
        for (NBCLIOptions.LoggerConfig classicConfigs : options.getClassicHistoConfigs()) {
            ActivityMetrics.addClassicHistos(sessionName, classicConfigs.pattern, classicConfigs.file, classicConfigs.interval);
        }

        // intentionally not shown for warn-only
        logger.info("console logging level is " + options.wantsConsoleLogLevel());

        if (options.getCommands().size() == 0) {
            System.out.println(loadHelpFile("commandline.md"));
            System.exit(0);
        }

        ScenariosExecutor executor = new ScenariosExecutor("executor-" + sessionName, 1);

        Scenario scenario = new Scenario(sessionName, options.getScriptingEngine(), options.getProgressSpec());
        ScriptBuffer buffer = new BasicScriptBuffer(
            options.getLogsDirectory()+ FileSystems.getDefault().getSeparator()+ "_scenario."+ scenario.getName() +".js"
        ).add(options.getCommands().toArray(new Cmd[0]));
        String scriptData = buffer.getParsedScript();
        Map<String,String> globalParams=buffer.getCombinedParams();

        if (options.wantsShowScript()) {
            System.out.println("// Rendered Script");
            System.out.println(scriptData);
            System.exit(0);
        }

        if (options.wantsEnableChart()) {
            logger.info("Charting enabled");
            scenario.enableCharting();
        } else {
            logger.info("Charting disabled");
        }


        // Execute Scenario!

        Level clevel = options.wantsConsoleLogLevel();
        Level llevel = Level.toLevel(options.getLogsLevel());
        if (llevel.toInt()>clevel.toInt()) {
            logger.info("raising scenario logging level to accommodate console logging level");
        }
        Level maxLevel = Level.toLevel(Math.min(clevel.toInt(), llevel.toInt()));

        scenario.addScriptText(scriptData);
        ScriptParams scriptParams = new ScriptParams();
        scriptParams.putAll(buffer.getCombinedParams());
        scenario.addScenarioScriptParams(scriptParams);
        ScenarioLogger sl = new ScenarioLogger(scenario)
            .setLogDir(options.getLogsDirectory())
            .setMaxLogs(options.getLogsMax())
            .setLevel(maxLevel)
            .setLogLevelOverrides(options.getLogLevelOverrides())
            .start();

        executor.execute(scenario, sl);

        while (true) {
            Optional<ScenarioResult> pendingResult = executor.getPendingResult(scenario.getName());
            if (pendingResult.isEmpty()) {
                LockSupport.parkNanos(100000000L);
            } else {
                break;
            }
        }

        ScenariosResults scenariosResults = executor.awaitAllResults();

        ActivityMetrics.closeMetrics(options.wantsEnableChart());
        scenariosResults.reportToLog();
        ShutdownManager.shutdown();

        if (scenariosResults.hasError()) {
            System.exit(2);
        } else {
            System.exit(0);
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

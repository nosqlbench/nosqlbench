package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogDumperUtility;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogImporterUtility;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.api.exceptions.BasicError;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import io.nosqlbench.engine.core.MarkdownDocInfo;
import io.nosqlbench.engine.core.ScenarioLogger;
import io.nosqlbench.engine.core.ScenariosResults;
import io.nosqlbench.engine.core.ShutdownManager;
import io.nosqlbench.engine.docker.DockerMetricsHelper;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.metrics.MetricReporters;
import io.nosqlbench.engine.core.script.MetricsMapper;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import io.nosqlbench.virtdata.userlibs.apps.VirtDataMainApp;
import io.nosqlbench.docsys.core.DocServerApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
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
        if (args.length>0 && args[0].toLowerCase().equals("virtdata")) {
            VirtDataMainApp.main(Arrays.copyOfRange(args,1,args.length));
            System.exit(0);
        }
        if (args.length>0 && args[0].toLowerCase().equals("docserver")) {
            DocServerApp.main(Arrays.copyOfRange(args,1,args.length));
            System.exit(0);
        }

        NBCLIOptions options = new NBCLIOptions(args);

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

        if (options.wantsWorkloads()) {
            printWorkloads();
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
        if (options.wantsDockerMetrics()){
            logger.info("Docker metrics is enabled. Docker must be installed for this to work");
            DockerMetricsHelper dmh= new DockerMetricsHelper();
            dmh.startMetrics();
            logger.info("Docker Containers are started, for grafana and prometheus, hit" +
                    "these urls in your browser: http://<host>:3000 and http://<host>:9090" +
                    "the default grafana creds are admin/admin");
            if (reportGraphiteTo != null){
                logger.warn(String.format("Docker metrics are enabled (--docker-metrics)" +
                    " but graphite reporting (--report-graphite-to) is set to %s \n" +
                    "usually only one of the two is configured.",
                    reportGraphiteTo));
            }else{
                //TODO: is this right?
                logger.info("Setting graphite reporting to localhost");
                reportGraphiteTo = "localhost:9109";
            }
        }

        if (reportGraphiteTo != null || options.wantsReportCsvTo() != null) {
            MetricReporters reporters = MetricReporters.getInstance();
            reporters.addRegistry("workloads", ActivityMetrics.getMetricRegistry());

            if (reportGraphiteTo != null) {
                reporters.addGraphite(reportGraphiteTo,  options.wantsMetricsPrefix());
            }
            if (options.wantsReportCsvTo() != null) {
                reporters.addCSVReporter(options.wantsReportCsvTo(), options.wantsMetricsPrefix());
            }
            reporters.start(10, options.getReportInterval());
        }

        String sessionName = new SessionNamer().format(options.getSessionName());

        if (options.wantsEnableChart()){
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

        ConsoleLogging.enableConsoleLogging(options.wantsConsoleLogLevel(), options.getConsoleLoggingPattern());
        // intentionally not shown for warn-only
        logger.info("console logging level is " + options.wantsConsoleLogLevel());

        if (options.getCommands().size() == 0) {
            System.out.println(loadHelpFile("commandline.md"));
            System.exit(0);
        }

        ScenariosExecutor executor = new ScenariosExecutor("executor-" + sessionName, 1);

        Scenario scenario = new Scenario(sessionName, options.getProgressSpec());
        NBCLIScriptAssembly.ScriptData scriptData = NBCLIScriptAssembly.assembleScript(options);
        if (options.wantsShowScript()) {
            System.out.println("// Rendered Script");
            System.out.println(scriptData.getScriptParamsAndText());
            System.exit(0);
        }

        if (options.wantsEnableChart()){
            logger.info("Charting enabled");
            scenario.enableCharting();
        } else{
            logger.info("Charting disabled");
        }

        scenario.addScenarioScriptParams(scriptData.getScriptParams());
        scenario.addScriptText(scriptData.getScriptTextIgnoringParams());
        ScenarioLogger sl = new ScenarioLogger(scenario)
                .setLogDir(options.getLogsDirectory())
                .setMaxLogs(options.getLogsMax())
                .setLevel(options.getLogsLevel())
                .setLogLevelOverrides(options.getLogLevelOverrides())
                .start();

        executor.execute(scenario, sl);
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

    public void printWorkloads() {
        List<NosqlBenchFiles.WorkloadDesc> workloads = NosqlBenchFiles.getWorkloadsWithScenarioScripts();
        for (NosqlBenchFiles.WorkloadDesc workload : workloads) {
            System.out.println("\n# from: "+ workload.getYamlPath());
            List<String> scenarioList = workload.getScenarioNames();
            String workloadName = workload.getYamlPath().replaceAll("\\.yaml", "") ;
            Set<String> templates = workload.getTemlpates();

            for (String scenario : scenarioList) {
                if (scenario.equals("default")) {
                    scenario = scenario +  " # same as running ./nb " + workloadName ;
                }
                System.out.println("  ./nb " + workloadName + " " + scenario);
            }
            if (templates.size()>0){
                System.out.println("# with the following optional parameters and defaults: ");
                templates.stream()
                    .map(x -> x.replaceAll(",","="))
                    .map(x -> x.replaceAll(":","="))
                    .map(x -> " # "+x)
                    .forEach(System.out::println);
            }
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

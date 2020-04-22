package io.nosqlbench.engine.cli;

import ch.qos.logback.classic.Level;
import io.nosqlbench.engine.api.metrics.IndicatorMode;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.util.Unit;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * No CLI parser lib is useful for command structures, it seems. So we have this instead, which is good enough.
 * If something better is needed later, this can be replaced.
 */
public class NBCLIOptions {

    private final static Logger logger = LoggerFactory.getLogger(NBCLIOptions.class);

    // Options which may contextualize other CLI options or commands.
    // These must be parsed first
    private static final String INCLUDE = "--include";
    private static final String METRICS_PREFIX = "--metrics-prefix";

    // Discovery
    private static final String HELP = "--help";
    private static final String LIST_METRICS = "--list-metrics";
    private static final String LIST_DRIVERS = "--list-drivers";
    private static final String LIST_ACTIVITY_TYPES = "--list-activity-types";
    private static final String LIST_WORKLOADS = "--list-workloads";
    private static final String LIST_SCENARIOS = "--list-scenarios";
    private static final String LIST_INPUT_TYPES = "--list-input-types";
    private static final String LIST_OUTPUT_TYPES = "--list-output-types";
    private static final String VERSION_COORDS = "--version-coords";
    private static final String VERSION = "--version";
    private static final String SHOW_SCRIPT = "--show-script";
    private static final String COPY = "--copy";

    // Execution
    private static final String SCRIPT = "script";
    private static final String ACTIVITY = "activity";
    private static final String SCENARIO = "scenario";
    private static final String RUN = "run";
    private static final String START = "start";
    private static final String FRAGMENT = "fragment";
    private static final String STOP = "stop";
    private static final String AWAIT = "await";
    private static final String WAIT_MILLIS = "waitmillis";
    private static final String EXPORT_CYCLE_LOG = "--export-cycle-log";
    private static final String IMPORT_CYCLE_LOG = "--import-cycle-log";

    // Execution Options

    private static final String SESSION_NAME = "--session-name";
    private static final String LOGS_DIR = "--logs-dir";
    private static final String LOGS_MAX = "--logs-max";
    private static final String LOGS_LEVEL = "--logs-level";
    private static final String DASH_V_INFO = "-v";
    private static final String DASH_VV_DEBUG = "-vv";
    private static final String DASH_VVV_TRACE = "-vvv";
    private static final String REPORT_INTERVAL = "--report-interval";
    private static final String REPORT_GRAPHITE_TO = "--report-graphite-to";
    private static final String REPORT_CSV_TO = "--report-csv-to";
    private static final String PROGRESS = "--progress";
    private static final String WITH_LOGGING_PATTERN = "--with-logging-pattern";
    private static final String LOG_HISTOGRAMS = "--log-histograms";
    private static final String LOG_HISTOSTATS = "--log-histostats";
    private static final String CLASSIC_HISTOGRAMS = "--classic-histograms";
    private final static String LOG_LEVEL_OVERRIDE = "--log-level-override";
    private final static String ENABLE_CHART = "--enable-chart";
    private final static String DOCKER_METRICS = "--docker-metrics";

    private static final String GRAALVM_ENGINE = "--graalvm";
    private static final String NASHORN_ENGINE = "--nashorn";


    public static final Set<String> RESERVED_WORDS = new HashSet<>() {{
        addAll(
            Arrays.asList(
                SCRIPT, ACTIVITY, SCENARIO, RUN, START,
                FRAGMENT, STOP, AWAIT, WAIT_MILLIS, LIST_ACTIVITY_TYPES, HELP
            )
        );
    }};

    private static final String DEFAULT_CONSOLE_LOGGING_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    private final LinkedList<Cmd> cmdList = new LinkedList<>();
    private int logsMax = 0;
    private boolean wantsVersionShort = false;
    private boolean wantsVersionCoords = false;
    private boolean wantsActivityHelp = false;
    private String wantsActivityHelpFor;
    private boolean wantsActivityTypes = false;
    private boolean wantsBasicHelp = false;
    private String reportGraphiteTo = null;
    private String reportCsvTo = null;
    private int reportInterval = 10;
    private String metricsPrefix = "nosqlbench.";
    private String wantsMetricsForActivity;
    private String sessionName = "";
    private boolean showScript = false;
    private Level consoleLevel = Level.WARN;
    private final List<String> histoLoggerConfigs = new ArrayList<>();
    private final List<String> statsLoggerConfigs = new ArrayList<>();
    private final List<String> classicHistoConfigs = new ArrayList<>();
    private String progressSpec = "console:1m";
    private String logsDirectory = "logs";
    private boolean wantsInputTypes = false;
    private boolean wantsMarkerTypes = false;
    private String[] rleDumpOptions = new String[0];
    private String[] cyclelogImportOptions = new String[0];
    private String consoleLoggingPattern = DEFAULT_CONSOLE_LOGGING_PATTERN;
    private String logsLevel = "INFO";
    private Map<String, Level> logLevelsOverrides = new HashMap<>();
    private boolean enableChart = false;
    private boolean dockerMetrics = false;
    private boolean wantsScenariosList = false;
    private String wantsToCopyWorkload = null;
    private boolean wantsWorkloadsList = false;
    private final List<String> wantsToIncludePaths = new ArrayList<>();
    private Scenario.Engine engine = Scenario.Engine.Graalvm;


    public NBCLIOptions(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {

        LinkedList<String> arglist = new LinkedList<>() {{
            addAll(Arrays.asList(args));
        }};

        if (arglist.peekFirst() == null) {
            wantsBasicHelp = true;
            return;
        }

        // Preprocess --include regardless of position
        LinkedList<String> nonincludes = new LinkedList<>();
        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                String wordToSplit = arglist.removeFirst();
                String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            if (INCLUDE.equals(word)) {
                arglist.removeFirst();
                String include = readWordOrThrow(arglist, "path to include");
                wantsToIncludePaths.add(include);
            } else if (METRICS_PREFIX.equals(word)) {
                arglist.removeFirst();
                metricsPrefix = arglist.removeFirst();
            } else {
                nonincludes.addLast(arglist.removeFirst());
            }
        }
        arglist = nonincludes;
        nonincludes = new LinkedList<>();

        PathCanonicalizer canonicalizer = new PathCanonicalizer(wantsIncludes());

        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                String wordToSplit = arglist.removeFirst();
                String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            switch (word) {
                case GRAALVM_ENGINE:
                    engine = Scenario.Engine.Graalvm;
                    arglist.removeFirst();
                    break;
                case NASHORN_ENGINE:
                    engine = Scenario.Engine.Nashorn;
                    arglist.removeFirst();
                    break;
                case SHOW_SCRIPT:
                    arglist.removeFirst();
                    showScript = true;
                    break;
                case LIST_METRICS:
                    arglist.removeFirst();
                    arglist.addFirst("start");
                    Cmd cmd = Cmd.parseArg(arglist,canonicalizer);
                    wantsMetricsForActivity = cmd.getArg("driver");
                    break;
                case SESSION_NAME:
                    arglist.removeFirst();
                    sessionName = readWordOrThrow(arglist, "a session name");
                    break;
                case LOGS_DIR:
                    arglist.removeFirst();
                    logsDirectory = readWordOrThrow(arglist, "a log directory");
                    break;
                case LOGS_MAX:
                    arglist.removeFirst();
                    logsMax = Integer.parseInt(readWordOrThrow(arglist, "max logfiles to keep"));
                    break;
                case LOGS_LEVEL:
                    arglist.removeFirst();
                    logsLevel = readWordOrThrow(arglist, "a log level");
                    break;
                case LOG_LEVEL_OVERRIDE:
                    arglist.removeFirst();
                    logLevelsOverrides = parseLogLevelOverrides(readWordOrThrow(arglist, "log levels in name:LEVEL,... format"));
                    break;
                case PROGRESS:
                    arglist.removeFirst();
                    progressSpec = readWordOrThrow(arglist, "a progress indicator, like 'log:1m' or 'screen:10s', or just 'log' or 'screen'");
                    break;
                case VERSION:
                    arglist.removeFirst();
                    wantsVersionShort = true;
                    break;
                case VERSION_COORDS:
                    arglist.removeFirst();
                    wantsVersionCoords = true;
                    break;
                case ENABLE_CHART:
                    arglist.removeFirst();
                    enableChart = true;
                    break;
                case DOCKER_METRICS:
                    arglist.removeFirst();
                    dockerMetrics = true;
                    break;
                case HELP:
                case "-h":
                case "help":
                    arglist.removeFirst();
                    if (arglist.peekFirst() == null) {
                        wantsBasicHelp = true;
                        logger.info("getting basic help");
                    } else {
                        wantsActivityHelp = true;
                        wantsActivityHelpFor = readWordOrThrow(arglist, "topic");
                    }
                    break;
                case EXPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    rleDumpOptions = readAllWords(arglist);
                    break;
                case IMPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    cyclelogImportOptions = readAllWords(arglist);
                    break;
                case LOG_HISTOGRAMS:
                    arglist.removeFirst();
                    String logto = arglist.removeFirst();
                    histoLoggerConfigs.add(logto);
                    break;
                case LOG_HISTOSTATS:
                    arglist.removeFirst();
                    String logStatsTo = arglist.removeFirst();
                    statsLoggerConfigs.add(logStatsTo);
                    break;
                case CLASSIC_HISTOGRAMS:
                    arglist.removeFirst();
                    String classicHistos = arglist.removeFirst();
                    classicHistoConfigs.add(classicHistos);
                    break;
                case REPORT_INTERVAL:
                    arglist.removeFirst();
                    reportInterval = Integer.parseInt(readWordOrThrow(arglist, "report interval"));
                    break;
                case REPORT_CSV_TO:
                    arglist.removeFirst();
                    reportCsvTo = arglist.removeFirst();
                    break;
                case REPORT_GRAPHITE_TO:
                    arglist.removeFirst();
                    reportGraphiteTo = arglist.removeFirst();
                    break;
                case LIST_DRIVERS:
                case LIST_ACTIVITY_TYPES:
                    arglist.removeFirst();
                    wantsActivityTypes = true;
                    break;
                case LIST_INPUT_TYPES:
                    arglist.removeFirst();
                    wantsInputTypes = true;
                    break;
                case LIST_OUTPUT_TYPES:
                    arglist.removeFirst();
                    wantsMarkerTypes = true;
                    break;
                case DASH_V_INFO:
                    consoleLevel = Level.INFO;
                    arglist.removeFirst();
                    break;
                case DASH_VV_DEBUG:
                    consoleLevel = Level.DEBUG;
                    arglist.removeFirst();
                    break;
                case DASH_VVV_TRACE:
                    consoleLevel = Level.TRACE;
                    arglist.removeFirst();
                    break;
                case WITH_LOGGING_PATTERN:
                    arglist.removeFirst();
                    consoleLoggingPattern = readWordOrThrow(arglist, "logging pattern");
                    break;
                case LIST_SCENARIOS:
                    arglist.removeFirst();
                    wantsScenariosList = true;
                    break;
                case LIST_WORKLOADS:
                    arglist.removeFirst();
                    wantsWorkloadsList = true;
                    break;
                case COPY:
                    arglist.removeFirst();
                    wantsToCopyWorkload = readWordOrThrow(arglist, "workload to copy");
                    break;
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }
        arglist = nonincludes;

        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                String wordToSplit = arglist.removeFirst();
                String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }
            Cmd cmd=null;
            switch (word) {
                case FRAGMENT:
                case SCRIPT:
                case START:
                case RUN:
                case AWAIT:
                case STOP:
                case WAIT_MILLIS:
                    cmd = Cmd.parseArg(arglist,canonicalizer);
                    cmdList.add(cmd);
                    break;
//                    cmd = Cmd.parseArg(arglist, this, "alias_to_await");
//                    String cmdName = arglist.removeFirst();
//                    String cmdParam = readWordOrThrow(arglist, "activity alias to await");
//                    assertNotParameter(cmdParam);
//                    assertNotReserved(cmdParam);
//                    cmdList.add(cmd);
//                    break;
//                    String stopCmdType = readWordOrThrow(arglist, "stop command");
//                    String activityToStop = readWordOrThrow(arglist, "activity alias to await");
//                    assertNotParameter(activityToStop);
//                    assertNotReserved(activityToStop);
//                    Cmd stopActivityCmd = Cmd.parseArg(arglist,this,"activity alias to stop");
//                    cmdList.add(stopActivityCmd);
//                    break;
//                    String waitMillisCmdType = readWordOrThrow(arglist, "wait millis");
//                    String millisCount = readWordOrThrow(arglist, "millis count");
//                    Long.parseLong(millisCount); // sanity check
//                    Cmd awaitMillisCmd = Cmd.parseArg(arglist,this,"milliseconds to wait");
//                    cmdList.add(awaitMillisCmd);
//                    break;
                default:
                    Optional<Content<?>> scriptfile = NBIO.local()
                        .prefix("scripts/auto")
                        .name(word)
                        .extension("js")
                        .first();

                    //Script
                    if (scriptfile.isPresent()) {
                        arglist.removeFirst();
                        arglist.addFirst("scripts/auto/" + word);
                        arglist.addFirst("script");
                        cmd = Cmd.parseArg(arglist,canonicalizer);
                        cmdList.add(cmd);
                    } else if (
                        NBCLIScenarioParser.isFoundWorkload(word, wantsIncludes())
                    ) {
                        NBCLIScenarioParser.parseScenarioCommand(arglist, RESERVED_WORDS, wantsIncludes());
                    } else {
                        throw new InvalidParameterException("unrecognized option:" + word);
                    }
                    break;
            }
        }
    }


    public String[] wantsIncludes() {
        return wantsToIncludePaths.toArray(new String[0]);
    }

    private Map<String, Level> parseLogLevelOverrides(String levelsSpec) {
        Map<String, Level> levels = new HashMap<>();
        Arrays.stream(levelsSpec.split("[,;]")).forEach(kp -> {
            String[] ll = kp.split(":");
            if (ll.length != 2) {
                throw new RuntimeException("Log level must have name:level format");
            }
            levels.put(ll[0], Level.toLevel(ll[1]));
        });
        return levels;
    }

    public Scenario.Engine getScriptingEngine() {
        return engine;
    }

    public List<LoggerConfig> getHistoLoggerConfigs() {
        List<LoggerConfig> configs = histoLoggerConfigs.stream().map(LoggerConfig::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, LOG_HISTOGRAMS);
        return configs;
    }

    public List<LoggerConfig> getStatsLoggerConfigs() {
        List<LoggerConfig> configs = statsLoggerConfigs.stream().map(LoggerConfig::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, LOG_HISTOSTATS);
        return configs;
    }

    public List<LoggerConfig> getClassicHistoConfigs() {
        List<LoggerConfig> configs = classicHistoConfigs.stream().map(LoggerConfig::new).collect(Collectors.toList());
        checkLoggerConfigs(configs, CLASSIC_HISTOGRAMS);
        return configs;
    }

    public List<Cmd> getCommands() {
        return cmdList;
    }

    public boolean wantsShowScript() {
        return showScript;
    }

    public boolean wantsVersionCoords() {
        return wantsVersionCoords;
    }

    public boolean isWantsVersionShort() {
        return wantsVersionShort;
    }

    public boolean wantsActivityTypes() {
        return wantsActivityTypes;
    }

    public boolean wantsTopicalHelp() {
        return wantsActivityHelp;
    }

    public String wantsTopicalHelpFor() {
        return wantsActivityHelpFor;
    }

    public boolean wantsBasicHelp() {
        return wantsBasicHelp;
    }

    public boolean wantsEnableChart() {
        return enableChart;
    }

    public boolean wantsDockerMetrics() {
        return dockerMetrics;
    }

    public int getReportInterval() {
        return reportInterval;
    }

    public String wantsReportGraphiteTo() {
        return reportGraphiteTo;
    }

    public String wantsMetricsPrefix() {
        return metricsPrefix;
    }

    public String wantsMetricsForActivity() {
        return wantsMetricsForActivity;
    }

    public String getSessionName() {
        return sessionName;
    }

    public Level wantsConsoleLogLevel() {
        return consoleLevel;
    }

    private void assertNotParameter(String scriptName) {
        if (scriptName.contains("=")) {
            throw new InvalidParameterException("script name must precede script arguments");
        }
    }

    private void assertNotReserved(String name) {
        if (RESERVED_WORDS.contains(name)) {
            throw new InvalidParameterException(name + " is a reserved word and may not be used here.");
        }
    }

    private String readWordOrThrow(LinkedList<String> arglist, String required) {
        if (arglist.peekFirst() == null) {
            throw new InvalidParameterException(required + " not found");
        }
        return arglist.removeFirst();
    }

    private String[] readAllWords(LinkedList<String> arglist) {
        String[] args = arglist.toArray(new String[0]);
        arglist.clear();
        return args;
    }

//    private Cmd parseScriptCmd(LinkedList<String> arglist) {
//        String cmdType = arglist.removeFirst();
//        String scriptName = readWordOrThrow(arglist, "script name");
//        assertNotReserved(scriptName);
//        assertNotParameter(scriptName);
//        Map<String, String> scriptParams = new LinkedHashMap<>();
//        while (arglist.size() > 0 && !RESERVED_WORDS.contains(arglist.peekFirst())
//            && arglist.peekFirst().contains("=")) {
//            String[] split = arglist.removeFirst().split("=", 2);
//            scriptParams.put(split[0], split[1]);
//        }
//        return new Cmd(CmdType.script, scriptName, scriptParams);
//    }


    public String getProgressSpec() {
        ProgressSpec spec = parseProgressSpec(this.progressSpec);// sanity check
        if (spec.indicatorMode == IndicatorMode.console
            && Level.INFO.isGreaterOrEqual(wantsConsoleLogLevel())) {
            logger.warn("Console is already logging info or more, so progress data on console is suppressed.");
            spec.indicatorMode = IndicatorMode.logonly;
        }
        return spec.toString();
    }

    private void checkLoggerConfigs(List<LoggerConfig> configs, String configName) {
        Set<String> files = new HashSet<>();
        configs.stream().map(LoggerConfig::getFilename).forEach(s -> {
            if (files.contains(s)) {
                logger.warn(s + " is included in " + configName + " more than once. It will only be included " +
                    "in the first matching config. Reorder your options if you need to control this.");
            }
            files.add(s);
        });
    }

    public String wantsReportCsvTo() {
        return reportCsvTo;
    }

    public String getLogsDirectory() {
        return logsDirectory;
    }

    public int getLogsMax() {
        return logsMax;
    }

    public String getLogsLevel() {
        return logsLevel;
    }

    public boolean wantsInputTypes() {
        return this.wantsInputTypes;
    }

    public boolean wantsMarkerTypes() {
        return wantsMarkerTypes;
    }

    public boolean wantsToDumpCyclelog() {
        return rleDumpOptions.length > 0;
    }

    public boolean wantsToImportCycleLog() {
        return cyclelogImportOptions.length > 0;
    }

    public String[] getCyclelogImportOptions() {
        return cyclelogImportOptions;
    }

    public String[] getCycleLogExporterOptions() {
        return rleDumpOptions;
    }

    public String getConsoleLoggingPattern() {
        return consoleLoggingPattern;
    }

    public Map<String, Level> getLogLevelOverrides() {
        return logLevelsOverrides;
    }

    public void setHistoLoggerConfigs(String pattern, String file, String interval) {
        //--log-histograms 'hdrdata.log:.*:2m'
        histoLoggerConfigs.add(String.format("%s:%s:%s", file, pattern, interval));
    }

    public boolean wantsScenariosList() {
        return wantsScenariosList;
    }

    public boolean wantsToCopyResource() {
        return wantsToCopyWorkload != null;
    }

    public String wantsToCopyResourceNamed() {
        return wantsToCopyWorkload;
    }

    public boolean wantsWorkloadsList() {
        return wantsWorkloadsList;
    }

    public static class LoggerConfig {
        public String file;
        public String pattern = ".*";
        public String interval = "30 seconds";

        public LoggerConfig(String histoLoggerSpec) {
            String[] words = histoLoggerSpec.split(":");
            switch (words.length) {
                case 3:
                    interval = words[2].isEmpty() ? interval : words[2];
                case 2:
                    pattern = words[1].isEmpty() ? pattern : words[1];
                case 1:
                    file = words[0];
                    if (file.isEmpty()) {
                        throw new RuntimeException("You must not specify an empty file here for logging data.");
                    }
                    break;
                default:
                    throw new RuntimeException(
                        LOG_HISTOGRAMS +
                            " options must be in either 'regex:filename:interval' or 'regex:filename' or 'filename' format"
                    );
            }
        }

        public String getFilename() {
            return file;
        }
    }

    private static class ProgressSpec {
        public String intervalSpec;
        public IndicatorMode indicatorMode;

        public String toString() {
            return indicatorMode.toString() + ":" + intervalSpec;
        }
    }

    private ProgressSpec parseProgressSpec(String interval) {
        ProgressSpec progressSpec = new ProgressSpec();
        String[] parts = interval.split(":");
        switch (parts.length) {
            case 2:
                Unit.msFor(parts[1]).orElseThrow(
                    () -> new RuntimeException("Unable to parse progress indicator indicatorSpec '" + parts[1] + "'")
                );
                progressSpec.intervalSpec = parts[1];
            case 1:
                progressSpec.indicatorMode = IndicatorMode.valueOf(parts[0]);
                break;
            default:
                throw new RuntimeException("This should never happen.");
        }
        return progressSpec;
    }


}

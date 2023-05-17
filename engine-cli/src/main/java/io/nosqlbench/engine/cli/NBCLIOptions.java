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

import io.nosqlbench.api.engine.util.Unit;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.logging.NBLogLevel;
import io.nosqlbench.api.system.NBEnvironment;
import io.nosqlbench.engine.api.metrics.IndicatorMode;
import io.nosqlbench.engine.cli.Cmd.CmdType;
import io.nosqlbench.engine.core.lifecycle.scenario.Scenario.Engine;
import io.nosqlbench.nb.annotations.Maturity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * No CLI parser lib is useful for command structures, it seems. So we have this instead, which is
 * good enough. If something better is needed later, this can be replaced.
 */
public class NBCLIOptions {

//    private final static Logger logger = LogManager.getLogger("OPTIONS");


    private static final String NB_STATE_DIR = "--statedir";
    private static final String NB_STATEDIR_PATHS = "$NBSTATEDIR:$PWD/.nosqlbench:$HOME/.nosqlbench";
    public static final String ARGS_FILE_DEFAULT = "$NBSTATEDIR/argsfile";
    private static final String INCLUDE = "--include";

    private static final String userHome = System.getProperty("user.home");


    private static final String METRICS_PREFIX = "--metrics-prefix";
    private static final String ANNOTATE_EVENTS = "--annotate";
    private static final String ANNOTATORS_CONFIG = "--annotators";

    // Enabled if the TERM env var is provided
    private static final String ANSI = "--ansi";

    private static final String DEFAULT_CHART_HDR_LOG_NAME = "hdrdata-for-chart.log";

    // Discovery
    private static final String HELP = "--help";
    private static final String LIST_COMMANDS = "--list-commands";
    private static final String LIST_METRICS = "--list-metrics";
    private static final String LIST_DRIVERS = "--list-drivers";
    private static final String LIST_ACTIVITY_TYPES = "--list-activity-types";
    private static final String LIST_SCRIPTS = "--list-scripts";
    private static final String LIST_WORKLOADS = "--list-workloads";
    private static final String LIST_SCENARIOS = "--list-scenarios";
    private static final String LIST_INPUT_TYPES = "--list-input-types";
    private static final String LIST_OUTPUT_TYPES = "--list-output-types";
    private static final String LIST_APPS = "--list-apps";
    private static final String VERSION_COORDS = "--version-coords";
    private static final String VERSION = "--version";
    private static final String SHOW_SCRIPT = "--show-script";
    private static final String COMPILE_SCRIPT = "--compile-script";
    private static final String SCRIPT_FILE = "--script-file";
    private static final String COPY = "--copy";
    private static final String SHOW_STACKTRACES = "--show-stacktraces";
    private static final String EXPERIMENTAL = "--experimental";
    private static final String MATURITY = "--maturity";

    // Execution
    private static final String EXPORT_CYCLE_LOG = "--export-cycle-log";
    private static final String IMPORT_CYCLE_LOG = "--import-cycle-log";
    private static final String HDR_DIGITS = "--hdr-digits";

    // Execution Options


    private static final String SESSION_NAME = "--session-name";
    private static final String LOGS_DIR = "--logs-dir";
    private static final String WORKSPACES_DIR = "--workspaces-dir";
    private static final String LOGS_MAX = "--logs-max";
    private static final String LOGS_LEVEL = "--logs-level";
    private static final String DASH_V_INFO = "-v";
    private static final String DASH_VV_DEBUG = "-vv";
    private static final String DASH_VVV_TRACE = "-vvv";
    private static final String REPORT_INTERVAL = "--report-interval";
    private static final String REPORT_GRAPHITE_TO = "--report-graphite-to";

    private static final String REPORT_PROMPUSH_TO = "--report-prompush-to";
    private static final String GRAPHITE_LOG_LEVEL = "--graphite-log-level";
    private static final String REPORT_CSV_TO = "--report-csv-to";
    private static final String REPORT_SUMMARY_TO = "--report-summary-to";
    private static final String REPORT_SUMMARY_TO_DEFAULT = "stdout:60,_LOGS_/_SESSION_.summary";
    private static final String PROGRESS = "--progress";
    private static final String WITH_LOGGING_PATTERN = "--with-logging-pattern";
    private static final String LOGGING_PATTERN = "--logging-pattern";
    private static final String CONSOLE_PATTERN = "--console-pattern";
    private static final String LOGFILE_PATTERN = "--logfile-pattern";
    private static final String LOG_HISTOGRAMS = "--log-histograms";
    private static final String LOG_HISTOSTATS = "--log-histostats";
    private static final String CLASSIC_HISTOGRAMS = "--classic-histograms";
    private static final String LOG_LEVEL_OVERRIDE = "--log-level-override";
    private static final String ENABLE_CHART = "--enable-chart";

    private static final String DOCKER_METRICS = "--docker-metrics";
    private static final String DOCKER_METRICS_AT = "--docker-metrics-at";
    private static final String DOCKER_GRAFANA_TAG = "--docker-grafana-tag";
    private static final String DOCKER_PROM_TAG = "--docker-prom-tag";
    private static final String DOCKER_PROM_RETENTION_DAYS = "--docker-prom-retention-days";

    private static final String GRAALJS_ENGINE = "--graaljs";

    private static final String DEFAULT_CONSOLE_PATTERN = "TERSE";
    private static final String DEFAULT_LOGFILE_PATTERN = "VERBOSE";
    private final static String ENABLE_DEDICATED_VERIFICATION_LOGGER = "--enable-dedicated-verification-logging";

    //    private static final String DEFAULT_CONSOLE_LOGGING_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";


    private final List<Cmd> cmdList = new ArrayList<>();
    private int logsMax;
    private boolean wantsVersionShort;
    private boolean wantsVersionCoords;
    private boolean wantsActivityHelp;
    private String wantsActivityHelpFor;
    private boolean wantsActivityTypes;
    private boolean wantsBasicHelp;
    private String reportGraphiteTo;
    private String reportPromPushTo;
    private String reportCsvTo;
    private int reportInterval = 10;
    private String metricsPrefix = "nosqlbench";
    private String wantsMetricsForActivity;
    private String sessionName = "";
    private boolean showScript;
    private NBLogLevel consoleLevel = NBLogLevel.WARN;
    private final List<String> histoLoggerConfigs = new ArrayList<>();
    private final List<String> statsLoggerConfigs = new ArrayList<>();
    private final List<String> classicHistoConfigs = new ArrayList<>();
    private String progressSpec = "console:1m";
    private String logsDirectory = "logs";
    private String workspacesDirectory = "workspaces";
    private boolean wantsInputTypes;
    private boolean wantsMarkerTypes;
    private String[] rleDumpOptions = new String[0];
    private String[] cyclelogImportOptions = new String[0];
    private String consoleLoggingPattern = NBCLIOptions.DEFAULT_CONSOLE_PATTERN;
    private String logfileLoggingPattern = NBCLIOptions.DEFAULT_LOGFILE_PATTERN;
    private NBLogLevel logsLevel = NBLogLevel.INFO;
    private Map<String, String> logLevelsOverrides = new HashMap<>();
    private boolean enableChart;
    private boolean dockerMetrics;
    private boolean wantsListScenarios;
    private boolean wantsListScripts;
    private String wantsToCopyWorkload;
    private boolean wantsWorkloadsList;
    private final List<String> wantsToIncludePaths = new ArrayList<>();
    private Engine engine = Engine.Graalvm;
    private int hdr_digits = 3;
    private String docker_grafana_tag = "7.3.4";
    private String docker_prom_tag = "latest";
    private boolean showStackTraces;
    private boolean compileScript;
    private String scriptFile;
    private String[] annotateEvents = {"ALL"};
    private String dockerMetricsHost;
    private String annotatorsConfig = "";
    private String statedirs = NBCLIOptions.NB_STATEDIR_PATHS;
    private Path statepath;
    private final List<String> statePathAccesses = new ArrayList<>();
    private final String hdrForChartFileName = NBCLIOptions.DEFAULT_CHART_HDR_LOG_NAME;
    private String dockerPromRetentionDays = "3650d";
    private String reportSummaryTo = NBCLIOptions.REPORT_SUMMARY_TO_DEFAULT;
    private boolean enableAnsi = (null != System.getenv("TERM")) && !System.getenv("TERM").isEmpty();
    private Maturity minMaturity = Maturity.Unspecified;
    private String graphitelogLevel = "info";
    private boolean wantsListCommands;
    private boolean wantsListApps;
    private boolean dedicatedVerificationLogger;

    public boolean isWantsListApps() {
        return this.wantsListApps;
    }

    public boolean getWantsListCommands() {
        return this.wantsListCommands;
    }

    public String getAnnotatorsConfig() {
        return this.annotatorsConfig;
    }


    public String getChartHdrFileName() {
        return this.hdrForChartFileName;
    }

    public String getDockerPromRetentionDays() {
        return dockerPromRetentionDays;
    }

    public String getReportSummaryTo() {
        return this.reportSummaryTo;
    }

    public void setWantsStackTraces(final boolean wantsStackTraces) {
        showStackTraces = wantsStackTraces;
    }

    public boolean isEnableAnsi() {
        return this.enableAnsi;
    }

    public String getLogfileLoggingPattern() {
        return this.logfileLoggingPattern;
    }

    public String getGraphiteLogLevel() {
        return graphitelogLevel;
    }

    public boolean isDedicatedVerificationLogger() {
        return this.dedicatedVerificationLogger;
    }

    public void enableDedicatedVerificationLogger() {
        this.dedicatedVerificationLogger = true;
    }

    public enum Mode {
        ParseGlobalsOnly,
        ParseAllOptions
    }

    public NBCLIOptions(final String[] args) {
        this(args, Mode.ParseAllOptions);
    }

    public NBCLIOptions(final String[] args, final Mode mode) {
        switch (mode) {
            case ParseGlobalsOnly:
                this.parseGlobalOptions(args);
                break;
            case ParseAllOptions:
                this.parseAllOptions(args);
                break;
        }
    }

    private LinkedList<String> parseGlobalOptions(final String[] args) {

        LinkedList<String> arglist = new LinkedList<>(Arrays.asList(args));
        if (null == arglist.peekFirst()) {
            this.wantsBasicHelp = true;
            return arglist;
        }

        // Process --include and --statedir, separately first
        // regardless of position
        LinkedList<String> nonincludes = new LinkedList<>();
        while (null != arglist.peekFirst()) {
            final String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                final String wordToSplit = arglist.removeFirst();
                final String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            switch (word) {
                case NBCLIOptions.NB_STATE_DIR:
                    arglist.removeFirst();
                    statedirs = this.readWordOrThrow(arglist, "nosqlbench global state directory");
                    break;
                case NBCLIOptions.INCLUDE:
                    arglist.removeFirst();
                    final String include = this.readWordOrThrow(arglist, "path to include");
                    this.wantsToIncludePaths.add(include);
                    break;
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }
        statedirs = (null != this.statedirs) ? statedirs : NBCLIOptions.NB_STATEDIR_PATHS;
        setStatePath();

        arglist = nonincludes;
        nonincludes = new LinkedList<>();

        // Now that statdirs is settled, auto load argsfile if it is present
        final NBCLIArgsFile argsfile = new NBCLIArgsFile();
        argsfile.reserved(NBCLICommandParser.RESERVED_WORDS);
        argsfile.preload("--argsfile-optional", NBCLIOptions.ARGS_FILE_DEFAULT);
        arglist = argsfile.process(arglist);

        // Parse all --argsfile... and other high level options

        while (null != arglist.peekFirst()) {
            final String word = arglist.peekFirst();
            if (word.startsWith("--") && word.contains("=")) {
                final String wordToSplit = arglist.removeFirst();
                final String[] split = wordToSplit.split("=", 2);
                arglist.offerFirst(split[1]);
                arglist.offerFirst(split[0]);
                continue;
            }

            switch (word) {
                // These options modify other options. They should be processed early.
                case NBCLIArgsFile.ARGS_FILE:
                case NBCLIArgsFile.ARGS_FILE_OPTIONAL:
                case NBCLIArgsFile.ARGS_FILE_REQUIRED:
                case NBCLIArgsFile.ARGS_PIN:
                case NBCLIArgsFile.ARGS_UNPIN:
                    if (null == this.statepath) this.setStatePath();
                    arglist = argsfile.process(arglist);
                    break;
                case NBCLIOptions.ANSI:
                    arglist.removeFirst();
                    final String doEnableAnsi = this.readWordOrThrow(arglist, "enable/disable ansi codes");
                    this.enableAnsi = doEnableAnsi.toLowerCase(Locale.ROOT).matches("enabled|enable|true");
                    break;
                case NBCLIOptions.DASH_V_INFO:
                    this.consoleLevel = NBLogLevel.INFO;
                    arglist.removeFirst();
                    break;
                case NBCLIOptions.DASH_VV_DEBUG:
                    this.consoleLevel = NBLogLevel.DEBUG;
                    showStackTraces = true;
                    arglist.removeFirst();
                    break;
                case NBCLIOptions.DASH_VVV_TRACE:
                    this.consoleLevel = NBLogLevel.TRACE;
                    showStackTraces = true;
                    arglist.removeFirst();
                    break;
                case NBCLIOptions.ENABLE_DEDICATED_VERIFICATION_LOGGER:
                    enableDedicatedVerificationLogger();
                    break;
                case NBCLIOptions.ANNOTATE_EVENTS:
                    arglist.removeFirst();
                    final String toAnnotate = this.readWordOrThrow(arglist, "annotated events");
                    this.annotateEvents = toAnnotate.split("\\\\s*,\\\\s*");
                    break;
                case NBCLIOptions.ANNOTATORS_CONFIG:
                    arglist.removeFirst();
                    annotatorsConfig = this.readWordOrThrow(arglist, "annotators config");
                    break;
                case NBCLIOptions.REPORT_GRAPHITE_TO:
                    arglist.removeFirst();
                    this.reportGraphiteTo = arglist.removeFirst();
                    break;
                case NBCLIOptions.REPORT_PROMPUSH_TO:
                    arglist.removeFirst();
                    this.reportPromPushTo = arglist.removeFirst();
                    break;
                case NBCLIOptions.GRAPHITE_LOG_LEVEL:
                    arglist.removeFirst();
                    this.graphitelogLevel = arglist.removeFirst();
                    break;
                case NBCLIOptions.METRICS_PREFIX:
                    arglist.removeFirst();
                    this.metricsPrefix = arglist.removeFirst();
                    break;
                case NBCLIOptions.WORKSPACES_DIR:
                    arglist.removeFirst();
                    this.workspacesDirectory = this.readWordOrThrow(arglist, "a workspaces directory");
                    break;
                case NBCLIOptions.DOCKER_PROM_TAG:
                    arglist.removeFirst();
                    this.docker_prom_tag = this.readWordOrThrow(arglist, "prometheus docker tag");
                    break;
                case NBCLIOptions.DOCKER_PROM_RETENTION_DAYS:
                    arglist.removeFirst();
                    this.dockerPromRetentionDays = this.readWordOrThrow(arglist, "prometheus retention (3650d by default)");
                    break;
                case NBCLIOptions.DOCKER_GRAFANA_TAG:
                    arglist.removeFirst();
                    this.docker_grafana_tag = this.readWordOrThrow(arglist, "grafana docker tag");
                    break;
                case NBCLIOptions.VERSION:
                    arglist.removeFirst();
                    this.wantsVersionShort = true;
                    break;
                case NBCLIOptions.VERSION_COORDS:
                    arglist.removeFirst();
                    this.wantsVersionCoords = true;
                    break;
                case NBCLIOptions.DOCKER_METRICS_AT:
                    arglist.removeFirst();
                    this.dockerMetricsHost = this.readWordOrThrow(arglist, "docker metrics host");
                    break;
                case NBCLIOptions.DOCKER_METRICS:
                    arglist.removeFirst();
                    this.dockerMetrics = true;
                    break;
                case NBCLIOptions.SESSION_NAME:
                    arglist.removeFirst();
                    this.sessionName = this.readWordOrThrow(arglist, "a session name");
                    break;
                case NBCLIOptions.LOGS_DIR:
                    arglist.removeFirst();
                    this.logsDirectory = this.readWordOrThrow(arglist, "a log directory");
                    break;
                case NBCLIOptions.LOGS_MAX:
                    arglist.removeFirst();
                    this.logsMax = Integer.parseInt(this.readWordOrThrow(arglist, "max logfiles to keep"));
                    break;
                case NBCLIOptions.LOGS_LEVEL:
                    arglist.removeFirst();
                    final String loglevel = this.readWordOrThrow(arglist, "a log level");
                    logsLevel = NBLogLevel.valueOfName(loglevel);
                    break;
                case NBCLIOptions.LOG_LEVEL_OVERRIDE:
                    arglist.removeFirst();
                    this.logLevelsOverrides = this.parseLogLevelOverrides(this.readWordOrThrow(arglist, "log levels in name:LEVEL,... format"));
                    break;
                case NBCLIOptions.CONSOLE_PATTERN:
                    arglist.removeFirst();
                    this.consoleLoggingPattern = this.readWordOrThrow(arglist, "console pattern");
                    break;
                case NBCLIOptions.LOGFILE_PATTERN:
                    arglist.removeFirst();
                    this.logfileLoggingPattern = this.readWordOrThrow(arglist, "logfile pattern");
                    break;
                case NBCLIOptions.WITH_LOGGING_PATTERN:
                case NBCLIOptions.LOGGING_PATTERN:
                    arglist.removeFirst();
                    final String pattern = this.readWordOrThrow(arglist, "console and logfile pattern");
                    this.consoleLoggingPattern = pattern;
                    this.logfileLoggingPattern = pattern;
                    break;
                case NBCLIOptions.SHOW_STACKTRACES:
                    arglist.removeFirst();
                    this.showStackTraces = true;
                    break;
                case NBCLIOptions.EXPERIMENTAL:
                    arglist.removeFirst();
                    arglist.addFirst("experimental");
                    arglist.addFirst("--maturity");
                    break;
                case NBCLIOptions.MATURITY:
                    arglist.removeFirst();
                    final String maturity = this.readWordOrThrow(arglist, "maturity of components to allow");
                    minMaturity = Maturity.valueOf(maturity.toLowerCase(Locale.ROOT));
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }

        return nonincludes;
    }

    private Path setStatePath() {
        if (0 < statePathAccesses.size())
            throw new BasicError("The state dir must be set before it is used by other\n" +
                    " options. If you want to change the statedir, be sure you do it before\n" +
                    " dependent options. These parameters were called before this --statedir:\n" +
                    this.statePathAccesses.stream().map(s -> "> " + s).collect(Collectors.joining("\n")));
        if (null != this.statepath) return statepath;

        final List<String> paths = NBEnvironment.INSTANCE.interpolateEach(":", this.statedirs);
        Path selected = null;

        for (final String pathName : paths) {
            final Path path = Path.of(pathName);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    selected = path;
                    break;
                }
                System.err.println("ERROR: possible state dir path is not a directory: '" + path + '\'');
            }
        }
        if (null == selected) selected = Path.of(paths.get(paths.size() - 1));

        if (!Files.exists(selected)) try {
            Files.createDirectories(
                    selected,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---"))
            );
        } catch (final IOException e) {
            throw new BasicError("Could not create state directory at '" + selected + "': " + e.getMessage());
        }

        NBEnvironment.INSTANCE.put(NBEnvironment.NBSTATEDIR, selected.toString());

        return selected;
    }

    private void parseAllOptions(final String[] args) {
        LinkedList<String> arglist = this.parseGlobalOptions(args);

        final PathCanonicalizer canonicalizer = new PathCanonicalizer(this.wantsIncludes());

        final LinkedList<String> nonincludes = new LinkedList<>();

        while (null != arglist.peekFirst()) {
            final String word = arglist.peekFirst();

            switch (word) {
                case NBCLIOptions.GRAALJS_ENGINE:
                    this.engine = Engine.Graalvm;
                    arglist.removeFirst();
                    break;
                case NBCLIOptions.COMPILE_SCRIPT:
                    arglist.removeFirst();
                    this.compileScript = true;
                    break;
                case NBCLIOptions.SHOW_SCRIPT:
                    arglist.removeFirst();
                    this.showScript = true;
                    break;
                case NBCLIOptions.LIST_COMMANDS:
                    arglist.removeFirst();
                    wantsListCommands = true;
                    break;
                case NBCLIOptions.LIST_METRICS:
                    arglist.removeFirst();
                    arglist.addFirst("start");
                    final Cmd cmd = Cmd.parseArg(arglist, canonicalizer);
                    this.wantsMetricsForActivity = cmd.getArg("driver");
                    break;
                case NBCLIOptions.HDR_DIGITS:
                    arglist.removeFirst();
                    this.hdr_digits = Integer.parseInt(this.readWordOrThrow(arglist, "significant digits"));
                    break;
                case NBCLIOptions.PROGRESS:
                    arglist.removeFirst();
                    this.progressSpec = this.readWordOrThrow(arglist, "a progress indicator, like 'log:1m' or 'screen:10s', or just 'log' or 'screen'");
                    break;
                case NBCLIOptions.ENABLE_CHART:
                    arglist.removeFirst();
                    this.enableChart = true;
                    break;
                case NBCLIOptions.HELP:
                case "-h":
                case "help":
                    arglist.removeFirst();
                    if (null == arglist.peekFirst()) this.wantsBasicHelp = true;
                    else {
                        this.wantsActivityHelp = true;
                        this.wantsActivityHelpFor = this.readWordOrThrow(arglist, "topic");
                    }
                    break;
                case NBCLIOptions.EXPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    this.rleDumpOptions = this.readAllWords(arglist);
                    break;
                case NBCLIOptions.IMPORT_CYCLE_LOG:
                    arglist.removeFirst();
                    this.cyclelogImportOptions = this.readAllWords(arglist);
                    break;
                case NBCLIOptions.LOG_HISTOGRAMS:
                    arglist.removeFirst();
                    final String logto = arglist.removeFirst();
                    this.histoLoggerConfigs.add(logto);
                    break;
                case NBCLIOptions.LOG_HISTOSTATS:
                    arglist.removeFirst();
                    final String logStatsTo = arglist.removeFirst();
                    this.statsLoggerConfigs.add(logStatsTo);
                    break;
                case NBCLIOptions.CLASSIC_HISTOGRAMS:
                    arglist.removeFirst();
                    final String classicHistos = arglist.removeFirst();
                    this.classicHistoConfigs.add(classicHistos);
                    break;
                case NBCLIOptions.REPORT_INTERVAL:
                    arglist.removeFirst();
                    this.reportInterval = Integer.parseInt(this.readWordOrThrow(arglist, "report interval"));
                    break;
                case NBCLIOptions.REPORT_CSV_TO:
                    arglist.removeFirst();
                    this.reportCsvTo = arglist.removeFirst();
                    break;
                case NBCLIOptions.REPORT_SUMMARY_TO:
                    arglist.removeFirst();
                    this.reportSummaryTo = this.readWordOrThrow(arglist, "report summary file");
                    break;
                case NBCLIOptions.LIST_DRIVERS:
                case NBCLIOptions.LIST_ACTIVITY_TYPES:
                    arglist.removeFirst();
                    this.wantsActivityTypes = true;
                    break;
                case NBCLIOptions.LIST_INPUT_TYPES:
                    arglist.removeFirst();
                    this.wantsInputTypes = true;
                    break;
                case NBCLIOptions.LIST_OUTPUT_TYPES:
                    arglist.removeFirst();
                    this.wantsMarkerTypes = true;
                    break;
                case NBCLIOptions.LIST_SCENARIOS:
                    arglist.removeFirst();
                    this.wantsListScenarios = true;
                    break;
                case NBCLIOptions.LIST_SCRIPTS:
                    arglist.removeFirst();
                    this.wantsListScripts = true;
                    break;
                case NBCLIOptions.LIST_WORKLOADS:
                    arglist.removeFirst();
                    this.wantsWorkloadsList = true;
                    break;
                case NBCLIOptions.LIST_APPS:
                    arglist.removeFirst();
                    this.wantsListApps = true;
                    break;
                case NBCLIOptions.SCRIPT_FILE:
                    arglist.removeFirst();
                    this.scriptFile = this.readWordOrThrow(arglist, "script file");
                    break;
                case NBCLIOptions.COPY:
                    arglist.removeFirst();
                    this.wantsToCopyWorkload = this.readWordOrThrow(arglist, "workload to copy");
                    break;
                default:
                    nonincludes.addLast(arglist.removeFirst());
            }
        }
        arglist = nonincludes;
        final Optional<List<Cmd>> commands = NBCLICommandParser.parse(arglist);
        if (commands.isPresent()) cmdList.addAll(commands.get());
        else {
            final String arg = arglist.peekFirst();
            Objects.requireNonNull(arg);
            final String helpmsg = """
                    Could not recognize command 'ARG'.
                    This means that all of the following searches for a compatible command failed:
                    1. commands: no scenario command named 'ARG' is known. (start, run, await, ...)
                    2. scripts: no auto script named './scripts/auto/ARG.js' in the local filesystem.
                    3. scripts: no auto script named 'scripts/auto/ARG.js' was found in the PROG binary.
                    4. workloads: no workload file named ARG[.yaml] was found in the local filesystem, even in include paths INCLUDES.
                    5. workloads: no workload file named ARG[.yaml] was bundled in PROG binary, even in include paths INCLUDES.
                    6. apps: no application named ARG was bundled in PROG.

                    You can discover available ways to invoke PROG by using the various --list-* commands:
                    [ --list-commands, --list-scripts, --list-workloads (and --list-scenarios), --list-apps ]
                    """
                    .replaceAll("ARG", arg)
                    .replaceAll("PROG", "nb5")
                    .replaceAll("INCLUDES", String.join(",", wantsIncludes()));
            throw new BasicError(helpmsg);

        }
    }


    public String[] wantsIncludes() {
        return this.wantsToIncludePaths.toArray(new String[0]);
    }

    private Map<String, String> parseLogLevelOverrides(final String levelsSpec) {
        final Map<String, String> levels = new HashMap<>();
        Arrays.stream(levelsSpec.split("[,;]")).forEach(kp -> {
            final String[] ll = kp.split(":");
            if (2 != ll.length) throw new RuntimeException("Log level must have name:level format");
            levels.put(ll[0], ll[1]);
        });
        return levels;
    }

    public Engine getScriptingEngine() {
        return this.engine;
    }

    public List<LoggerConfigData> getHistoLoggerConfigs() {
        final List<LoggerConfigData> configs =
                this.histoLoggerConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        this.checkLoggerConfigs(configs, NBCLIOptions.LOG_HISTOGRAMS);
        return configs;
    }

    public List<LoggerConfigData> getStatsLoggerConfigs() {
        final List<LoggerConfigData> configs =
                this.statsLoggerConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        this.checkLoggerConfigs(configs, NBCLIOptions.LOG_HISTOSTATS);
        return configs;
    }

    public List<LoggerConfigData> getClassicHistoConfigs() {
        final List<LoggerConfigData> configs =
                this.classicHistoConfigs.stream().map(LoggerConfigData::new).collect(Collectors.toList());
        this.checkLoggerConfigs(configs, NBCLIOptions.CLASSIC_HISTOGRAMS);
        return configs;
    }

    public Maturity allowMinMaturity() {
        return this.minMaturity;
    }

    public List<Cmd> getCommands() {
        return this.cmdList;
    }

    public boolean wantsShowScript() {
        return this.showScript;
    }

    public boolean wantsCompileScript() {
        return this.compileScript;
    }

    public boolean wantsVersionCoords() {
        return this.wantsVersionCoords;
    }

    public boolean isWantsVersionShort() {
        return this.wantsVersionShort;
    }

    public boolean wantsActivityTypes() {
        return this.wantsActivityTypes;
    }

    public boolean wantsTopicalHelp() {
        return this.wantsActivityHelp;
    }

    public boolean wantsStackTraces() {
        return this.showStackTraces;
    }

    public String wantsTopicalHelpFor() {
        return this.wantsActivityHelpFor;
    }

    public boolean wantsBasicHelp() {
        return this.wantsBasicHelp;
    }

    public boolean wantsEnableChart() {
        return this.enableChart;
    }

    public boolean wantsDockerMetrics() {
        return this.dockerMetrics;
    }

    public String wantsDockerMetricsAt() {
        return this.dockerMetricsHost;
    }

    public int getReportInterval() {
        return this.reportInterval;
    }

    public String wantsReportGraphiteTo() {
        return this.reportGraphiteTo;
    }

    public String wantsReportPromPushTo() {
        return this.reportPromPushTo;
    }

    public String wantsMetricsPrefix() {
        return this.metricsPrefix;
    }

    public String wantsMetricsForActivity() {
        return this.wantsMetricsForActivity;
    }

    public String getSessionName() {
        return this.sessionName;
    }

    public NBLogLevel getConsoleLogLevel() {
        return this.consoleLevel;
    }

    private String readWordOrThrow(final LinkedList<String> arglist, final String required) {
        if (null == arglist.peekFirst())
            throw new InvalidParameterException(required + " is required after this option");
        return arglist.removeFirst();
    }

    private String[] readAllWords(final LinkedList<String> arglist) {
        final String[] args = arglist.toArray(new String[0]);
        arglist.clear();
        return args;
    }

    public int getHdrDigits() {
        return this.hdr_digits;
    }

    public String getProgressSpec() {
        final ProgressSpec spec = this.parseProgressSpec(progressSpec);// sanity check
        //                System.err.println("Console is already logging info or more, so progress data on console is " +
        //                        "suppressed.");
        if (IndicatorMode.console == spec.indicatorMode)
            if (consoleLevel.isGreaterOrEqualTo(NBLogLevel.INFO)) spec.indicatorMode = IndicatorMode.logonly;
            else //                System.err.println("Command line includes script calls, so progress data on console is " +
                //                        "suppressed.");
                if (cmdList.stream().anyMatch(cmd -> CmdType.script == cmd.getCmdType()))
                    spec.indicatorMode = IndicatorMode.logonly;
        return spec.toString();
    }

    private void checkLoggerConfigs(final List<LoggerConfigData> configs, final String configName) {
        final Set<String> files = new HashSet<>();
        configs.stream().map(LoggerConfigData::getFilename).forEach(s -> {
            if (files.contains(s))
                System.err.println(s + " is included in " + configName + " more than once. It will only be " +
                        "included " +
                        "in the first matching config. Reorder your options if you need to control this.");
            files.add(s);
        });
    }

    public String wantsReportCsvTo() {
        return this.reportCsvTo;
    }

    public Path getLogsDirectory() {
        return Path.of(this.logsDirectory);
    }

    public int getLogsMax() {
        return this.logsMax;
    }

    public NBLogLevel getScenarioLogLevel() {
        return this.logsLevel;
    }

    public boolean wantsInputTypes() {
        return wantsInputTypes;
    }

    public String getScriptFile() {
        if (null == scriptFile) return this.logsDirectory + File.separator + "_SESSION_" + ".js";

        String expanded = this.scriptFile;
        if (!expanded.startsWith(File.separator)) expanded = this.getLogsDirectory() + File.separator + expanded;
        return expanded;
    }

    public boolean wantsMarkerTypes() {
        return this.wantsMarkerTypes;
    }

    public boolean wantsToDumpCyclelog() {
        return 0 < rleDumpOptions.length;
    }

    public boolean wantsToImportCycleLog() {
        return 0 < cyclelogImportOptions.length;
    }

    public String[] getCyclelogImportOptions() {
        return this.cyclelogImportOptions;
    }

    public String[] getCycleLogExporterOptions() {
        return this.rleDumpOptions;
    }

    public String getConsoleLoggingPattern() {
        return this.consoleLoggingPattern;
    }

    public Map<String, String> getLogLevelOverrides() {
        return this.logLevelsOverrides;
    }

    public void setHistoLoggerConfigs(final String pattern, final String file, final String interval) {
        //--log-histograms 'hdrdata.log:.*:2m'
        this.histoLoggerConfigs.add(String.format("%s:%s:%s", file, pattern, interval));
    }

    public boolean wantsScenariosList() {
        return this.wantsListScenarios;
    }

    public boolean wantsListScripts() {
        return this.wantsListScripts;
    }

    public boolean wantsToCopyResource() {
        return null != wantsToCopyWorkload;
    }

    public String wantsToCopyResourceNamed() {
        return this.wantsToCopyWorkload;
    }

    public boolean wantsWorkloadsList() {
        return this.wantsWorkloadsList;
    }

    public String getDockerGrafanaTag() {
        return this.docker_grafana_tag;
    }

    public String getDockerPromTag() {
        return this.docker_prom_tag;
    }

    public static class LoggerConfigData {
        public String file;
        public String pattern = ".*";
        public String interval = "30 seconds";

        public LoggerConfigData(final String histoLoggerSpec) {
            final String[] words = histoLoggerSpec.split(":");
            switch (words.length) {
                case 3:
                    this.interval = words[2].isEmpty() ? this.interval : words[2];
                case 2:
                    this.pattern = words[1].isEmpty() ? this.pattern : words[1];
                case 1:
                    this.file = words[0];
                    if (this.file.isEmpty())
                        throw new RuntimeException("You must not specify an empty file here for logging data.");
                    break;
                default:
                    throw new RuntimeException(
                            NBCLIOptions.LOG_HISTOGRAMS +
                                    " options must be in either 'regex:filename:interval' or 'regex:filename' or 'filename' format"
                    );
            }
        }

        public String getFilename() {
            return this.file;
        }
    }

    private static class ProgressSpec {
        public String intervalSpec;
        public IndicatorMode indicatorMode;

        public String toString() {
            return this.indicatorMode.toString() + ':' + this.intervalSpec;
        }
    }

    private ProgressSpec parseProgressSpec(final String interval) {
        final ProgressSpec progressSpec = new ProgressSpec();
        final String[] parts = interval.split(":");
        switch (parts.length) {
            case 2:
                Unit.msFor(parts[1]).orElseThrow(
                        () -> new RuntimeException("Unable to parse progress indicator indicatorSpec '" + parts[1] + '\'')
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

package io.nosqlbench.engine.core.logging;

import io.nosqlbench.nb.api.logging.NBLogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.nio.file.attribute.*;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

//@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
//@Order(50)
// Can't use plugin injection, since we need a tailored instance before logging is fully initialized

/**
 * This is a custom programmatic logger config handler which allows for a variety of
 * logging features to be controlled at runtime.
 *
 * @see <a href="https://logging.apache.org/log4j/2.x/manual/layouts.html#Pattern_Layout">Pattern Layout</a>
 */
public class LoggerConfig extends ConfigurationFactory {

    public static Map<String, String> STANDARD_FORMATS = Map.of(
        "TERSE", "%8r %-5level [%t] %-12logger{0} %msg%n%throwable",
        "VERBOSE", "%d{DEFAULT}{GMT} [%t] %logger %-5level: %msg%n%throwable",
        "TERSE-ANSI", "%8r %highlight{%-5level} %style{%C{1.} [%t] %-12logger{0}} %msg%n%throwable",
        "VERBOSE-ANSI", "%d{DEFAULT}{GMT} [%t] %highlight{%logger %-5level}: %msg%n%throwable"
    );

    /**
     * Some included libraries are spammy and intefere with normal diagnostic visibility, so
     * we squelch them to some reasonable level so they aren't a nuisance.
     */
    public static Map<String, Level> BUILTIN_OVERRIDES = Map.of(
        "oshi.util", Level.INFO
    );

    /**
     * ArgsFile
     * Environment
     * NBCLIOptions
     */
    private static final String DEFAULT_CONSOLE_PATTERN = "%7r %-5level [%t] %-12logger{0} %msg%n%throwable";
    private String consolePattern = DEFAULT_CONSOLE_PATTERN;
    private NBLogLevel consoleLevel = NBLogLevel.DEBUG;

    private static final String DEFAULT_LOGFILE_PATTERN = "%d{DEFAULT}{GMT} [%t] %-5level: %msg%n";
    private final String logfilePattern = DEFAULT_LOGFILE_PATTERN;
    private NBLogLevel fileLevel = NBLogLevel.DEBUG;

    private Map<String, String> logLevelOverrides = new LinkedHashMap<>();
    private Path loggerDir = Path.of("logs");
    private String sessionName;
    private int maxLogfiles = 100;
    private String logfileLocation;
    private boolean ansiEnabled;


    public LoggerConfig() {
    }

    public LoggerConfig setAnsiEnabled(boolean ansiEnabled) {
        this.ansiEnabled = ansiEnabled;
        return this;
    }

    public LoggerConfig setConsoleLevel(NBLogLevel level) {
        this.consoleLevel = level;
        return this;
    }

    public LoggerConfig setLogfileLevel(NBLogLevel level) {
        this.fileLevel = level;
        return this;
    }

    /**
     * Ensure that what is shown in the logfile includes at a minimum,
     * everything that is shown on console, but allow it to show more
     * if configured to do so.
     */
    private NBLogLevel getEffectiveFileLevel() {
        if (fileLevel.isGreaterOrEqualTo(consoleLevel)) {
            return fileLevel;
        } else {
            return consoleLevel;
        }
    }

    public LoggerConfig setMaxLogs(int maxLogfiles) {
        this.maxLogfiles = maxLogfiles;
        return this;
    }


    Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {

        Level internalLoggingStatusThreshold = Level.ERROR;
        Level builderThresholdLevel = Level.INFO;

        Level fileLevel = Level.valueOf(getEffectiveFileLevel().toString());
        Level consoleLevel = Level.valueOf(this.consoleLevel.toString());

        // configure the ROOT logger the same way as the File level
        // this is because the fileLevel is supposed to show more than the console

        // therefore, it is very important that the ROOT level is as much specific as possible
        // because NB code and especially third party libraries may rely on logging guards (if logger.isDebugEnabled())
        // to reduce memory allocations and resource waste due to debug/trace logging
        // if you set ROOT to ALL or to TRACE then you will trigger the execution of trace/debugging code
        // that will affect performances and impact on the measurements made with NB
        Level rootLoggingLevel = fileLevel;
        RootLoggerComponentBuilder rootBuilder = builder.newRootLogger(rootLoggingLevel);

        builder.setConfigurationName(name);

        builder.setStatusLevel(internalLoggingStatusThreshold);

        builder.add(
            builder.newFilter(
                "ThresholdFilter",
                Filter.Result.ACCEPT,
                Filter.Result.NEUTRAL
            ).addAttribute("level", builderThresholdLevel)
        );

        // CONSOLE appender
        AppenderComponentBuilder appenderBuilder =
            builder.newAppender("console", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);

        appenderBuilder.add(builder.newLayout("PatternLayout")
            .addAttribute("pattern", consolePattern));

//        appenderBuilder.add(
//                builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
//                        .addAttribute("marker", "FLOW"));

        builder.add(appenderBuilder);

        // Log4J internal logging
        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
            .add(builder.newAppenderRef("console"))
            .addAttribute("additivity", false));

        if (sessionName != null) {

            if (!Files.exists(loggerDir)) {
                try {
                    Files.createDirectories(loggerDir);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create logger directory:" + loggerDir);
                }
            }

            // LOGFILE appender
            LayoutComponentBuilder logfileLayout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", logfilePattern);

            String filebase = getSessionName().replaceAll("\\s", "_");
            String logfilePath = loggerDir.resolve(filebase + ".log").toString();
            this.logfileLocation = logfilePath;
            String archivePath = loggerDir.resolve(filebase + "-TIMESTAMP.log.gz").toString()
                .replaceAll("TIMESTAMP", "%d{MM-dd-yy}");

            ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));

            AppenderComponentBuilder logsAppenderBuilder =
                builder.newAppender("SCENARIO_APPENDER", RollingFileAppender.PLUGIN_NAME)
                    .addAttribute("fileName", logfilePath)
                    .addAttribute("filePattern", archivePath)
                    .addAttribute("append", false)
                    .add(logfileLayout)
                    .addComponent(triggeringPolicy);
            builder.add(logsAppenderBuilder);

            rootBuilder.add(
                builder.newAppenderRef("SCENARIO_APPENDER")
                    .addAttribute("level", fileLevel)
            );
        }

        rootBuilder.add(
            builder.newAppenderRef("console")
                .addAttribute("level",
                    consoleLevel
                )
        );

        builder.add(rootBuilder);

        BUILTIN_OVERRIDES.forEach((k, v) -> {
            builder.add(builder.newLogger(k, v)
                .add(builder.newAppenderRef("console"))
                .add(builder.newAppenderRef("SCENARIO_APPENDER"))
                .addAttribute("additivity", true));
        });

        logLevelOverrides.forEach((k, v) -> {
            Level olevel = Level.valueOf(v);
            builder.add(builder.newLogger(k, olevel)
                .add(builder.newAppenderRef("console"))
                .add(builder.newAppenderRef("SCENARIO_APPENDER"))
                .addAttribute("additivity", true));
        });

        BuiltConfiguration builtConfig = builder.build();
        return builtConfig;
    }

    private String getSessionName() {
        return sessionName;
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
        return getConfiguration(loggerContext, source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation) {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{"*"};
    }

    public void activate() {
        if (!Files.exists(loggerDir)) {
            try {
                FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwxrwx---")
                );
                Path directory = Files.createDirectory(loggerDir, attrs);
            } catch (Exception e) {
                throw new RuntimeException("Error while creating directory " + loggerDir.toString() + ": " + e.getMessage(), e);
            }
        }
        ConfigurationFactory.setConfigurationFactory(this);
    }

    public LoggerConfig setConsolePattern(String consoleLoggingPattern) {

        consoleLoggingPattern= (ansiEnabled && STANDARD_FORMATS.containsKey(consoleLoggingPattern+"-ANSI"))
            ? consoleLoggingPattern+"-ANSI" : consoleLoggingPattern;

        this.consolePattern = STANDARD_FORMATS.getOrDefault(consoleLoggingPattern, consoleLoggingPattern);
        return this;
    }

    public LoggerConfig setLogfilePattern(String logfileLoggingPattern) {
        logfileLoggingPattern= (logfileLoggingPattern.endsWith("-ANSI") && STANDARD_FORMATS.containsKey(logfileLoggingPattern))
            ? logfileLoggingPattern.substring(logfileLoggingPattern.length()-5) : logfileLoggingPattern;

        this.logfileLocation = STANDARD_FORMATS.getOrDefault(logfileLoggingPattern, logfileLoggingPattern);
        return this;
    }

    public LoggerConfig getLoggerLevelOverrides(Map<String, String> logLevelOverrides) {
        this.logLevelOverrides = logLevelOverrides;
        return this;
    }

    public Map<String, String> getLogLevelOverrides() {
        return logLevelOverrides;
    }

    public LoggerConfig setSessionName(String sessionName) {
        this.sessionName = sessionName;
        return this;
    }

    public LoggerConfig purgeOldFiles(Logger logger) {
        if (maxLogfiles == 0) {
            logger.debug("Not purging old files, since maxLogFiles is 0.");
            return this;
        }


        File[] files = loggerDir.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".log") || pathname.getPath().endsWith(".log.gz");
            }
        });

        if (files == null) {
            return this;
        }

        List<File> filesList = Arrays.asList(files);
        int remove = filesList.size() - maxLogfiles;
        if (remove <= 0) {
            return this;
        }

        List<File> toDelete = filesList.stream()
            .sorted(fileTimeComparator)
            .limit(remove)
            .collect(Collectors.toList());

        for (File file : toDelete) {
            logger.info("removing extra logfile: " + file.getPath());
            if (!file.delete()) {
                logger.warn("unable to delete: " + file);
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this;
    }

    private static final Comparator<File> fileTimeComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return Long.compare(o1.lastModified(), o2.lastModified());
        }
    };


    public String getLogfileLocation() {
        return logfileLocation;
    }

    public LoggerConfig setLogsDirectory(Path logsDirectory) {
        this.loggerDir = logsDirectory;
        return this;
    }
}

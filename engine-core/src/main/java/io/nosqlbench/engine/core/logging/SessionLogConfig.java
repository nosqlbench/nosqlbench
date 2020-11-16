package io.nosqlbench.engine.core.logging;

import io.nosqlbench.nb.api.logging.NBLogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.spi.LoggerContext;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SessionLogConfig implements ScenarioLogger {

    //    private static final String LOG_PATTERN = "%d{DEFAULT}{GMT} [%t] %-5level: %msg%n";
    private static final String DEFAULT_CONSOLE_PATTERN = "%7r %-5level [%t] %-12logger{0} %msg%n%throwable";
    private final String session;
    private String consolePattern = DEFAULT_CONSOLE_PATTERN;
    //    private final Scenario scenario;
    private Path loggerDir = Path.of("logs");
    private NBLogLevel logLevel;
    private int maxLogfiles = 100;
    private Map<String, String> logLevelOverrides = new LinkedHashMap<>();

    public SessionLogConfig(String session) {
        this.session = session;
    }

    public ScenarioLogger setConsolePattern(String consolePattern) {
        this.consolePattern = consolePattern;
        return this;
    }

    public String getConsolePattern() {
        return consolePattern;
    }

    public void configure() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.WARN);

        builder.setConfigurationName("ScenarioLogger");

        // Append simple format to stdout

        AppenderComponentBuilder appenderBuilder =
                builder.newAppender("console", "CONSOLE")
                        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);

        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", consolePattern));

        builder.add(appenderBuilder);


// create a rolling file appender

        Path scenarioLog = composeSessionLogName(loggerDir, session);

        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
//                .addAttribute("pattern", "%d [%t] %-5level: %msg%n");
                .addAttribute("pattern", "%date %level [%thread] %logger{10} [%file:%line] %msg%n");

        appenderBuilder = builder.newAppender("scenariolog", "File")
                .addAttribute("fileName", scenarioLog.getFileName())
                .addAttribute("append", false)
                .add(layoutBuilder);
        builder.add(appenderBuilder);

        // Assemble the scenario logger instance
        Level level = Level.valueOf(logLevel.toString());
        builder.add(builder.newRootLogger(level)
                .add(builder.newAppenderRef("console"))
                .add(builder.newAppenderRef("scenariolog"))
                .addAttribute("additivity", true));

        logLevelOverrides.forEach((k, v) -> {
            Level olevel = Level.valueOf(v);
            builder.add(builder.newLogger(k, olevel)
                    .add(builder.newAppenderRef("console"))
                    .add(builder.newAppenderRef("scenariolog"))
                    .addAttribute("additivity", true));
        });

        LoggerContext ctx = Configurator.initialize(builder.build());
    }

    public static Path composeSessionLogName(Path loggerDir, String session) {
        String logfilePath = loggerDir.toString() + File.separator + ".log";
        Path resolved = loggerDir.resolve(session.replaceAll("\\s", "_") + ".log");
        return resolved;
    }

    @Override
    public ScenarioLogger setLogDir(Path logDir) {
        this.loggerDir = logDir;
        return this;
    }

    @Override
    public Path getLogDir() {
        return this.loggerDir;
    }

    @Override
    public ScenarioLogger setMaxLogs(int maxLogfiles) {
        this.maxLogfiles = maxLogfiles;
        return this;
    }

    @Override
    public void purgeOldFiles(Logger logger) {
        if (maxLogfiles == 0) {
            logger.debug("Not purging old files, since maxLogFiles is 0.");
            return;
        }


        File[] files = loggerDir.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".log");
            }
        });
        if (files == null) {
            return;
        }

        List<File> filesList = Arrays.asList(files);
        int remove = filesList.size() - maxLogfiles;
        if (remove <= 0) {
            return;
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
    }

    private static final Comparator<File> fileTimeComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return Long.compare(o1.lastModified(), o2.lastModified());
        }
    };

    @Override
    public ScenarioLogger setLevel(NBLogLevel levelname) {
        this.logLevel = levelname;
        return this;
    }

    @Override
    public NBLogLevel getLevel() {
        return logLevel;
    }

    @Override
    public ScenarioLogger start() {
        if (!Files.exists(loggerDir)) {
            try {
                Files.createDirectories(loggerDir);
            } catch (Exception e) {
                throw new RuntimeException("Unable to create logger directory:" + loggerDir);
            }
        }

        configure();
        org.apache.logging.log4j.Logger logger = LogManager.getLogger("LOGGER");

        purgeOldFiles(logger);
        return this;
    }

    @Override
    public ScenarioLogger setLogLevelOverrides(Map<String, String> logLevelOverrides) {
        this.logLevelOverrides = logLevelOverrides;
        return this;
    }
}

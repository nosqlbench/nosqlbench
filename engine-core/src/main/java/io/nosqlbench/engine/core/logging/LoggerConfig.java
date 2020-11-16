package io.nosqlbench.engine.core.logging;

import io.nosqlbench.nb.api.logging.NBLogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.net.URI;

//@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
//@Order(50)
// Can't use plugin injection, since we need a tailored instance before logging
public class LoggerConfig extends ConfigurationFactory {

    private static final String LOG_PATTERN = "%d{DEFAULT}{GMT} [%t] %-5level: %msg%n";
    private static final String CONSOLE_PATTERN = "%7r %-5level [%t] %-12logger{0} %msg%n%throwable";
    public static final Level ROOT_LOG_LEVEL = Level.ALL;
    private final NBLogLevel consoleLevel;
    private final NBLogLevel fileLevel;

    public LoggerConfig(NBLogLevel consoleLevel, NBLogLevel fileLevel) {
        this.consoleLevel = consoleLevel;
        this.fileLevel = fileLevel;
    }

    Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {

        Level internalLoggingStatusThreshold = Level.ERROR;
        Level builderThresholdLevel = Level.INFO;
//        Level rootLoggingLevel = Level.INFO;

        builder.setConfigurationName(name);

        builder.setStatusLevel(internalLoggingStatusThreshold);

//        builder.add(
//                builder.newFilter(
//                        "ThresholdFilter",
//                        Filter.Result.ACCEPT,
//                        Filter.Result.NEUTRAL
//                ).addAttribute("level", builderThresholdLevel)
//        );

        // CONSOLE appender
        AppenderComponentBuilder appenderBuilder =
                builder.newAppender("Stdout", "CONSOLE")
                        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);

        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", CONSOLE_PATTERN));

//        appenderBuilder.add(
//                builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
//                        .addAttribute("marker", "FLOW")
//        );
        builder.add(appenderBuilder);

        // Log4J internal logging
        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
                .add(builder.newAppenderRef("Stdout"))
                .addAttribute("additivity", false));


        // LOGFILE appender

        LayoutComponentBuilder logfileLayout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", LOG_PATTERN);

        ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));

        AppenderComponentBuilder logsAppenderBuilder =
                builder.newAppender("LOGS_APPENDER", RollingFileAppender.PLUGIN_NAME)
                        .addAttribute("fileName", "logs/irengine.log")
                        .addAttribute("filePattern", "logs/irengine-%d{MM-dd-yy}.log.gz")
                        .addAttribute("append", true)
                        .add(logfileLayout)
                        .addComponent(triggeringPolicy);
        builder.add(logsAppenderBuilder);

//        LoggerComponentBuilder payloadBuilder =
//                builder.newLogger(PAYLOADS, Level.TRACE)
//                        .addAttribute("additivity", false);
//
//
//        if (consoleConfig.isPaylaodsEnabled()) {
//            payloadBuilder = payloadBuilder.add(builder.newAppenderRef("Stdout").addAttribute("level",
//                    consoleConfig.getLogLevel()));
//        }
//        if (fileConfig.isPaylaodsEnabled()) {
//            payloadBuilder = payloadBuilder.add(builder.newAppenderRef("LOGS_APPENDER").addAttribute("level",
//                    fileConfig.getLogLevel()));
//        }
//        builder.add(payloadBuilder);


//        LoggerComponentBuilder stacktracesBuilder =
//                builder.newLogger(STACKTRACES, Level.TRACE)
//                        .addAttribute("additivity", false);
//        if (consoleConfig.isStackTracesEnabled()) {
//            stacktracesBuilder = payloadBuilder.add(builder.newAppenderRef("Stdout").addAttribute("level",
//                    consoleConfig.getLogLevel()));
//        }
//        if (fileConfig.isStackTracesEnabled()) {
//            stacktracesBuilder = payloadBuilder.add(builder.newAppenderRef("LOGS_APPENDER").addAttribute("level",
//                    fileConfig.getLogLevel()));
//        }
//        builder.add(stacktracesBuilder);

        // ROOT logging and appender
        builder.add(
                builder.newRootLogger(ROOT_LOG_LEVEL)
                        .add(
                                builder.newAppenderRef("Stdout")
                                        .addAttribute("level", Level.valueOf(consoleLevel.toString()))
                        )
                        .add(
                                builder.newAppenderRef("LOGS_APPENDER")
                                        .addAttribute("level", Level.valueOf(fileLevel.toString()))
                        )
        );


        return builder.build();
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
}

/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import io.nosqlbench.engine.core.script.Scenario;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class ScenarioLogger {

    private final Scenario scenario;
    private File loggerDir = new File("logs");
    private int maxLogfiles = 10;
    private Level logLevel = Level.INFO;
    private Map<String, Level> logLevelOverrides = new HashMap<>();

    public ScenarioLogger(Scenario scenario) {
        this.scenario = scenario;
    }

    public ScenarioLogger setLogDir(String logDir) {
        this.loggerDir = new File(logDir);
        return this;
    }

    public ScenarioLogger setMaxLogs(int maxLogfiles) {
        this.maxLogfiles = maxLogfiles;
        return this;
    }

    public ScenarioLogger setLevel(Level level) {
        this.logLevel = level;
        return this;
    }

    public ScenarioLogger setLevel(String levelname) {
        this.logLevel = Level.toLevel(levelname);
        return this;
    }

    public ScenarioLogger start() {

        if (!loggerDir.exists()) {
            if (!loggerDir.mkdirs()) {
                throw new RuntimeException("Unable to create logger directory:" + loggerDir.getPath());
            }
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //List<LoggerContextListener> copyOfListenerList = loggerContext.getCopyOfListenerList();

        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(loggerContext);

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
//        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");

        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(loggerContext);
        ple.start();

        String scenarioLog = loggerDir.getPath() + File.separator + scenario.getName()+".log";
        scenarioLog = scenarioLog.replaceAll("\\s","_");
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setFile(scenarioLog);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(loggerContext);
        fileAppender.setImmediateFlush(true);
        System.err.println("Logging to " + scenarioLog);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
        logger.addAppender(fileAppender);

        logLevelOverrides.forEach((loggerName,loggingLevel) -> {
            logger.debug("Overriding log level for " + loggerName + " to " + loggingLevel);
            Logger toOverride = (Logger) LoggerFactory.getLogger(loggerName);
            toOverride.setLevel(loggingLevel);
            toOverride.debug("Log level was set to " + loggingLevel +
                    " by CLI option.");
        });

        logger.setLevel(logLevel);
        logger.setAdditive(true); /* set to true if root should log too */

        purgeOldFiles(logger);

        return this;
    }

    private void purgeOldFiles(Logger logger) {
        if (maxLogfiles==0) {
            logger.debug("Not purging old files, since maxLogFiles is 0.");
            return;
        }


        File[] files = loggerDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith(".log");
            }
        });
        if (files==null) {
            return;
        }

        List<File> filesList = Arrays.asList(files);
        int remove = filesList.size() - maxLogfiles;
        if (remove<=0) {
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

    private static Comparator<File> fileTimeComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return Long.compare(o1.lastModified(),o2.lastModified());
        }
    };


    public ScenarioLogger setLogLevelOverrides(Map<String, Level> logLevelOverrides) {
        this.logLevelOverrides = logLevelOverrides;
        return this;
    }

    public String getLogDir() {
        return this.loggerDir.toString();
    }
}

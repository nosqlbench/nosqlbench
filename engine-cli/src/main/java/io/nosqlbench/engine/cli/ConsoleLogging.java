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

package io.nosqlbench.engine.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;

public class ConsoleLogging {

    public static void enableConsoleLogging(Level level, String loggingPattern) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //List<LoggerContextListener> copyOfListenerList = loggerContext.getCopyOfListenerList();

        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(loggerContext);

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern(loggingPattern);
        ple.setContext(loggerContext);
        ple.start();
        ca.setEncoder(ple);
        LevelFilter<ILoggingEvent> levelFilter = new LevelFilter<>(level);
        levelFilter.start();
        ca.addFilter(levelFilter);
        ca.start();

        Logger root = loggerContext.getLogger("ROOT");
        root.addAppender(ca);
        root.setLevel(Level.TRACE);
    }

    private static class LevelFilter<E> extends AbstractMatcherFilter<E> {

        private final Level filterLevel;

        public LevelFilter(Level filterLevel) {
            this.filterLevel = filterLevel;
        }
        @Override
        public FilterReply decide(Object event) {
            if (!isStarted()) {
                return FilterReply.NEUTRAL;
            }
            LoggingEvent loggingEvent = (LoggingEvent) event;
            if (((LoggingEvent) event).getLevel().isGreaterOrEqual(filterLevel)) {
                return FilterReply.ACCEPT;
            }
            return FilterReply.DENY;
        }
    }
}

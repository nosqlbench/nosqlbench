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

package io.nosqlbench.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as collection of test mocks.
 */
public class NBMock {

    // Registration of test logger provided with appender added for test inspection of logging.
    public static LogAppender registerTestLogger(String appenderName, Logger logger, Level level) {
        LogAppender mockedAppender = new NBMock.LogAppender(appenderName);
        mockedAppender.start();
        logger.addAppender(mockedAppender);
        logger.setLevel(level);
        return (LogAppender) logger.getAppenders().get(appenderName);
    }

    // Appender implementation associated to a specific logger; used to obtain log specific entries in tests.
    public static class LogAppender extends AbstractAppender {
        private final List<String> entries = new ArrayList<>(1);

        public LogAppender(String name) {
            super(name, null, null, false, new Property[0]);
        }

        @Override
        public void append(LogEvent event) {
            entries.add(event.getMessage().getFormattedMessage());
        }

        public String getFirstEntry() {
            if (entries.isEmpty()) {
                return null;
            }
            return entries.get(0);
        }

        public List<String> getEntries() {
            return entries;
        }
        
        public void cleanup(Logger logger) {
            this.stop();
            entries.clear();

            if (logger != null) {
                logger.removeAppender(this);
            }
        }
    }
}

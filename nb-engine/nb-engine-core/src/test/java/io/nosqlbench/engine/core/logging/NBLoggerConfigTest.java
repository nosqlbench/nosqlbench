package io.nosqlbench.engine.core.logging;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.logging.NBLogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.NullAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NBLoggerConfigTest {

    @TempDir
    Path tempDir;

    private LoggerContext loggerContext;
    private Configuration originalConfiguration;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LogManager.getContext(false);
        originalConfiguration = loggerContext.getConfiguration();
        assumeSymlinkSupport(tempDir);
    }

    @AfterEach
    void tearDown() {
        try {
            ConfigurationFactory.resetConfigurationFactory();
        } finally {
            if (loggerContext != null && originalConfiguration != null) {
                loggerContext.stop();
                loggerContext.start(originalConfiguration);
            }
        }
    }

    @Test
    void defaultConfigurationIsNoopUntilActivation() {
        Configuration defaultConfig = loggerContext.getConfiguration();
        assertThat(defaultConfig.getName()).isEqualTo("nosqlbench-default");
        assertThat(defaultConfig.getRootLogger().getLevel()).isEqualTo(Level.OFF);

        Appender noopAppender = defaultConfig.getAppender("noop");
        assertThat(noopAppender).isInstanceOf(NullAppender.class);

        Logger logger = LogManager.getLogger("noop-test");
        assertThat(logger.isInfoEnabled()).isFalse();
        assertThat(logger.isErrorEnabled()).isFalse();

        NBLoggerConfig config = new NBLoggerConfig()
            .setSessionName("noopuntilactivation")
            .setLogsDirectory(tempDir)
            .setConsoleLevel(NBLogLevel.INFO)
            .setLogfileLevel(NBLogLevel.INFO);

        config.activate();

        Configuration activatedConfig = loggerContext.getConfiguration();
        assertThat(activatedConfig.getName()).isEqualTo("nosqlbench-logging");
        assertThat(activatedConfig.getRootLogger().getLevel()).isEqualTo(Level.INFO);

        Appender sessionAppender = activatedConfig.getAppender(NBLoggerConfig.SESSION_APPENDER);
        assertThat(sessionAppender).isInstanceOf(FileAppender.class);
        assertThat(((FileAppender) sessionAppender).isStarted()).isTrue();
    }

    @Test
    void activateAppliesCustomPatternAndReconfiguresContext() throws IOException {
        NBLoggerConfig config = new NBLoggerConfig()
            .setSessionName("patternsession")
            .setLogsDirectory(tempDir)
            .setConsoleLevel(NBLogLevel.INFO)
            .setLogfileLevel(NBLogLevel.INFO)
            .setLogfilePattern("%msg%n");

        Configuration before = loggerContext.getConfiguration();

        config.activate();

        Configuration after = loggerContext.getConfiguration();
        assertThat(after.getName()).isEqualTo("nosqlbench-logging");
        assertThat(after).isNotSameAs(before);

        Appender appender = after.getAppender(NBLoggerConfig.SESSION_APPENDER);
        assertThat(appender).isInstanceOf(FileAppender.class);
        FileAppender fileAppender = (FileAppender) appender;
        assertThat(extractPattern(fileAppender)).isEqualTo("%msg%n");

        Path sessionFile = tempDir.resolve("patternsession_session.log");
        assertThat(sessionFile).exists();

        Path sessionLink = tempDir.resolve("session.log");
        assertThat(sessionLink).isSymbolicLink();
        assertThat(Files.readSymbolicLink(sessionLink)).isEqualTo(Path.of("patternsession_session.log"));
        assertThat(config.getSessionLinkPath()).isEqualTo(sessionLink);
    }

    @Test
    void activationCreatesAuxiliarySymlinks() throws IOException {
        NBLoggerConfig config = new NBLoggerConfig()
            .setSessionName("symlinksession")
            .setLogsDirectory(tempDir)
            .setConsoleLevel(NBLogLevel.INFO)
            .setLogfileLevel(NBLogLevel.DEBUG)
            .setDedicatedVerificationLogger(true);

        config.activate();

        Path verifyLink = tempDir.resolve("verify.log");
        assertThat(verifyLink).isSymbolicLink();
        assertThat(Files.readSymbolicLink(verifyLink)).isEqualTo(Path.of("symlinksession_session_verify.log"));
        assertThat(tempDir.resolve("symlinksession_session_verify.log")).exists();

        Path runtimeLink = tempDir.resolve("runtime.log");
        assertThat(runtimeLink).isSymbolicLink();
        assertThat(Files.readSymbolicLink(runtimeLink)).isEqualTo(Path.of("symlinksession_session_runtime.log"));
        assertThat(tempDir.resolve("symlinksession_session_runtime.log")).exists();
    }

    private void assumeSymlinkSupport(Path directory) {
        Path probeLink = directory.resolve("symlink-probe");
        try {
            Files.deleteIfExists(probeLink);
            Files.createSymbolicLink(probeLink, Path.of("probe-target"));
            Files.deleteIfExists(probeLink);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.assumeTrue(false, "Symlinks are not supported: " + e.getMessage());
        }
    }

    private String extractPattern(FileAppender appender) {
        PatternLayout layout = (PatternLayout) appender.getLayout();
        try {
            Method method = PatternLayout.class.getMethod("getConversionPattern");
            Object result = method.invoke(layout);
            return result == null ? null : result.toString();
        } catch (NoSuchMethodException ignored) {
            try {
                Field field = PatternLayout.class.getDeclaredField("conversionPattern");
                field.setAccessible(true);
                Object value = field.get(layout);
                return value == null ? null : value.toString();
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException("Unable to extract pattern from PatternLayout", ex);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to extract pattern from PatternLayout", e);
        }
    }
}

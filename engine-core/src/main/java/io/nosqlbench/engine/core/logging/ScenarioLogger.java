package io.nosqlbench.engine.core.logging;

import io.nosqlbench.nb.api.logging.NBLogLevel;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;

public interface ScenarioLogger {

    ScenarioLogger setLogDir(Path logDir);

    Path getLogDir();

    ScenarioLogger setMaxLogs(int maxLogfiles);

    void purgeOldFiles(Logger logger);

    ScenarioLogger setLevel(NBLogLevel levelname);

    NBLogLevel getLevel();

    ScenarioLogger start();

    ScenarioLogger setLogLevelOverrides(Map<String, String> logLevelOverrides);

}

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

package io.nosqlbench.engine.extensions.histostatslogger;

import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponent;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

public class HistoStatsPlugin {

    private final Logger logger;
    private final NBComponent baseComponent;

    public HistoStatsPlugin(Logger logger, NBComponent baseComponent) {
        this.logger = logger;
        this.baseComponent = baseComponent;
    }

    public void logHistoStats(String sessionComment, String pattern, String filename , String interval) {
        throw new RuntimeException("replace me after merge");
//        ActivityMetrics.addStatsLogger(sessionComment, pattern, filename, interval);
    }
}

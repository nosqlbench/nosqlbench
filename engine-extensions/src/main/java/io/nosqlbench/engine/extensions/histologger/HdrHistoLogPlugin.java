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

package io.nosqlbench.engine.extensions.histologger;

import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.components.NBBaseComponent;
import com.codahale.metrics.MetricRegistry;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

public class HdrHistoLogPlugin {
    private final Logger logger;
    private final NBBaseComponent baseComponent;
    private final ScriptContext scriptContext;

    public HdrHistoLogPlugin(Logger logger, NBBaseComponent baseComponent, ScriptContext scriptContext) {
        this.logger = logger;
        this.baseComponent = baseComponent;
        this.scriptContext = scriptContext;
    }

    public void logHistoIntervals(String session, String pattern, String filename, String interval) {
        // TODO: metrics
//        ActivityMetrics.addHistoLogger(session, pattern, filename, interval);
    }
}

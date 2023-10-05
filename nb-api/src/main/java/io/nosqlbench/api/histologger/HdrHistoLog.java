/*
 * Copyright (c) 2022-2023 nosqlbench
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
 *
 */

package io.nosqlbench.api.histologger;

import io.nosqlbench.api.engine.metrics.HistoIntervalLogger;
import io.nosqlbench.api.engine.util.Unit;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponent;

import java.io.File;
import java.util.regex.Pattern;

public class HdrHistoLog extends NBBaseComponent {

    public HdrHistoLog(NBComponent baseComponent) {
        super(baseComponent);
    }

    public void logHistoIntervals(String session, String pattern, String filename, String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", session);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() ->
            new RuntimeException("Unable to parse interval spec:'" + interval + '\''));

        HistoIntervalLogger histoIntervalLogger =
            new HistoIntervalLogger(session, logfile, compiledPattern, intervalMillis);
    }
}

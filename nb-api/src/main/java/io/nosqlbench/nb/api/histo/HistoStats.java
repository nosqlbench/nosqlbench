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
 */

package io.nosqlbench.nb.api.histo;

import io.nosqlbench.nb.api.engine.metrics.HistoStatsLogger;
import io.nosqlbench.nb.api.engine.util.Unit;
import io.nosqlbench.nb.api.components.NBBaseComponent;
import io.nosqlbench.nb.api.components.NBComponent;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HistoStats extends NBBaseComponent {

    public HistoStats(NBComponent baseComponent) {
        super(baseComponent);
    }

    public void logHistoStats(String sessionComment, String pattern, String filename , String interval) {
        if (filename.contains("_SESSION_")) {
            filename = filename.replace("_SESSION_", sessionComment);
        }
        Pattern compiledPattern = Pattern.compile(pattern);
        File logfile = new File(filename);
        long intervalMillis = Unit.msFor(interval).orElseThrow(() ->
            new RuntimeException("Unable to parse interval spec:" + interval + '\''));

        HistoStatsLogger histoStatsLogger =
            new HistoStatsLogger(this, sessionComment, logfile, compiledPattern, intervalMillis, TimeUnit.NANOSECONDS);
        this.attachChild(histoStatsLogger);
        //TODO: Use create().histogram() instead?
    }
}

/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioSummary {
    private final static Logger logger = LogManager.getLogger(ScenarioSummary.class);
    private static void doReportSummaries(NBComponent scenario, final String reportSummaryTo, final ExecutionMetricsResult result, Map<String,String> subs) {
        final List<PrintStream> fullChannels = new ArrayList<>();
        final List<PrintStream> briefChannels = new ArrayList<>();
        final String[] destinationSpecs = reportSummaryTo.split(", *");

        for (final String spec : destinationSpecs)
            if ((null != spec) && !spec.isBlank()) {
                final String[] split = spec.split(":", 2);
                final String summaryTo = split[0];
                final long summaryWhen = (2 == split.length) ? (Long.parseLong(split[1]) * 1000L) : 0;

                PrintStream out = null;
                switch (summaryTo.toLowerCase()) {
                    case "console":
                    case "stdout":
                        out = System.out;
                        break;
                    case "stderr":
                        out = System.err;
                        break;
                    default:
                        String outName = summaryTo;
                        for (String s : subs.keySet()) {
                            outName = outName.replaceAll("_"+s.toUpperCase()+"_",subs.get(s));
                        }
                        try {
                            out = new PrintStream(new FileOutputStream(outName));
                            break;
                        } catch (final FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                }


                if (result.getElapsedMillis() > summaryWhen) fullChannels.add(out);
                else {
                    logger.debug("Summarizing counting metrics only to {} with scenario duration of {}ms (<{})", spec, summaryWhen, summaryWhen);
                    briefChannels.add(out);
                }
            }
        for (PrintStream fullChannel : fullChannels) {
            result.reportMetricsSummaryTo(scenario, fullChannel);
        }
    }

}

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

package io.nosqlbench.engine.core.lifecycle;

import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.core.progress.StateCapable;
import io.nosqlbench.engine.api.metrics.IndicatorMode;
import io.nosqlbench.engine.api.metrics.PeriodicRunnable;
import io.nosqlbench.engine.api.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ActivityProgressIndicator implements Runnable {

    private final static Logger logger = LogManager.getLogger("PROGRESS");
    private final String indicatorSpec;
    private final ScenarioController sc;
    private PeriodicRunnable<ActivityProgressIndicator> runnable;
    private IndicatorMode indicatorMode = IndicatorMode.console;
    private final Set<String> seen = new HashSet<>();

    private long intervalMillis = 1L;

    public ActivityProgressIndicator(ScenarioController sc, String indicatorSpec) {
        this.sc = sc;
        this.indicatorSpec = indicatorSpec;
        start();
    }

    public void start() {
        parseProgressSpec(indicatorSpec);
        this.runnable = new PeriodicRunnable<>(intervalMillis, this);
        runnable.startDaemonThread();
    }

    private void parseProgressSpec(String interval) {
        String[] parts = interval.split(":");
        switch (parts.length) {
            case 2:
                intervalMillis = Unit.msFor(parts[1]).orElseThrow(
                    () -> new RuntimeException("Unable to parse progress indicator indicatorSpec '" + parts[1] + "'")
                );
            case 1:
                try {
                    indicatorMode = IndicatorMode.valueOf(parts[0]);
                } catch (IllegalArgumentException ie) {
                    throw new RuntimeException(
                        "No such IndicatorMode exists for --progress: choose one of console or logonly." +
                            " If you need to specify an interval such as 10m, then you must use --progress logonly:10m or --progress console:10m");
                }
                break;
            default:
                throw new RuntimeException("This should never happen.");
        }
    }

    @Override
    public void run() {
        Collection<ProgressMeterDisplay> progressMeterDisplays = sc.getProgressMeters();
        for (ProgressMeterDisplay meter : progressMeterDisplays) {

            boolean lastReport = false;
            if (meter.getRatioComplete() >= 1.0d ||
                (meter instanceof StateCapable sc && sc.getRunState() == RunState.Finished)) {
                if (seen.contains(meter.getProgressName())) {
                    continue;
                } else {
                    seen.add(meter.getProgressName());
                    lastReport = true;
                }
            }

            String progress =
                meter.getSummary() + (lastReport ? " (last report)" : "");

            switch (indicatorMode) {
                case console:
                    System.out.println(progress);
                case logonly:
                    logger.info(progress);
            }
        }
    }

    private String formatProgress(double progress) {
        if (Double.isNaN(progress)) {
            return "Unknown";
        }
        return String.format(Locale.US, "%3.2f%%", (100.0 * progress));
    }

    public String toString() {
        return "ProgressIndicator/" + this.indicatorSpec;
    }

}

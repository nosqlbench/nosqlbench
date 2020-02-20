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

package io.nosqlbench.activitycore;

import io.nosqlbench.activityapi.core.ProgressMeter;
import io.nosqlbench.activityapi.core.RunState;
import io.nosqlbench.core.ScenarioController;
import io.nosqlbench.metrics.IndicatorMode;
import io.nosqlbench.metrics.PeriodicRunnable;
import io.nosqlbench.util.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ProgressIndicator implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ProgressIndicator.class);
    private final String indicatorSpec;
    private final ScenarioController sc;
    private PeriodicRunnable<ProgressIndicator> runnable;
    private IndicatorMode indicatorMode = IndicatorMode.console;
    private Set<String> seen = new HashSet<>();

    private long intervalMillis=1L;

    public ProgressIndicator(ScenarioController sc, String indicatorSpec) {
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
                        () -> new RuntimeException("Unable to parse progress indicator indicatorSpec '" + parts[1] +"'")
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
        Collection<ProgressMeter> progressMeters = sc.getProgressMeters();
        for (ProgressMeter meter : progressMeters) {

            boolean lastReport=false;
            if (meter.getProgress()>=1.0d || meter.getProgressState()== RunState.Finished) {
                if (seen.contains(meter.getProgressName())) {
                    continue;
                } else {
                    seen.add(meter.getProgressName());
                    lastReport=true;
                }
            }

            String progress = meter.getProgressName() + ": " + formatProgress(meter.getProgress()) + "/" + meter.getProgressState() +
                    " (details: " + meter.getProgressDetails()+")" + (lastReport ? " (last report)" : "");

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

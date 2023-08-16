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

package io.nosqlbench.engine.api.activityapi.core.progress;

import java.time.Instant;
import java.util.Locale;

public interface ProgressMeterDisplay {
    String getProgressName();

    Instant getStartTime();

    default double getMinValue() {
        return 0.0d;
    }
    double getMaxValue();

    double getCurrentValue();

    default double getRatioComplete() {
        return (getCurrentValue() - getMinValue()) / (getMaxValue() - getMinValue());
    }

    default String getRatioSummary() {
        double progress = getRatioComplete();
        return format(progress);
    }

    static String format(double value) {
        if (Double.isNaN(value)) {
            return "Unknown";
        }
        String formatted = String.format(Locale.US, "%03.0f%%", (100.0 * value));
        return formatted;
    }

    default String getSummary() {

        StringBuilder legend = new StringBuilder(getProgressName()).append(" (");
        StringBuilder values = new StringBuilder("(");

        if (this instanceof RemainingMeter remaining) {
            legend.append("remaining,");
            values.append(String.format("%.0f,",remaining.getRemainingCount()));
        }
        if (this instanceof ActiveMeter active) {
            legend.append("active,");
            values.append(String.format("%.0f,",active.getActiveOps()));
        }
        if (this instanceof CompletedMeter completed) {
            legend.append("completed,");
            values.append(String.format("%.0f,",completed.getCompletedCount()));
        }
        legend.setLength(legend.length()-1);
        values.setLength(values.length()-1);

//        legend.append(" ETA:").append(getETAInstant());
        String formatted = legend.append(")=").append(values).append(") ").append(getRatioSummary()).toString();
        return formatted;
    }

    default long getProgressETAMillis() {
        long now = System.currentTimeMillis();
        double elapsed = now - getStartTime().toEpochMilli();

        double completed = getRatioComplete();
        double rate = completed / elapsed;

        double remainingWork = getMaxValue() - getCurrentValue();
        double remainingTime = remainingWork / rate;

        return (long) remainingTime;
    }

    default Instant getETAInstant() {
        return Instant.ofEpochMilli(System.currentTimeMillis()+getProgressETAMillis());
    }


}


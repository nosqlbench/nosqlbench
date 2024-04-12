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

package io.nosqlbench.engine.api.activityapi.core.progress;

import java.util.Locale;

public record ProgressSummary(
        String name,
        double pending,
        double current,
        double complete
) {

    private String getRatioSummary() {
        double progress = complete / (pending+complete);
        if (Double.isNaN(progress)) {
            return "Unknown";
        }
        return String.format(Locale.US, "%3.2f%%", (100.0 * progress));
    }

    public String toString() {
        StringBuilder legend = new StringBuilder(name).append(" (");
        StringBuilder values = new StringBuilder("(");

        legend.append("pending,");
        values.append(String.format("%.0f,",pending));
        legend.append("current,");
        values.append(String.format("%.0f,", current));
        legend.append("complete,");
        values.append(String.format("%.0f,",complete));
        legend.setLength(legend.length()-1);
        values.setLength(values.length()-1);

//        legend.append(" ETA:").append(getETAInstant());
        String formatted = legend.append(")=").append(values).append(") ").append(getRatioSummary()).toString();
        return formatted;
    }

}

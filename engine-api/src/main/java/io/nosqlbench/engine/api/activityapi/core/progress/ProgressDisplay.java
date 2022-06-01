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

package io.nosqlbench.engine.api.activityapi.core.progress;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ProgressDisplay {
    private final ProgressMeter[] meters;

    public ProgressDisplay(ProgressMeter... meters) {
        this.meters = meters;
    }

    public static CharSequence of(ProgressMeter... meters) {
        return new ProgressDisplay(meters).toString();
    }

    public String toString() {
        if (meters.length == 0) {
            return "";
        } else if (meters.length == 1) {
            return meters[0].getSummary();
        } else {
            double total = 0d;
            for (ProgressMeter meter : meters) {
                total += meter.getMaxValue();
            }
            return "PROGRESS:" + ProgressMeter.format(total / meters.length) + " (" +
                Arrays.stream(meters).map(ProgressMeter::getSummary).collect(Collectors.joining(","));
        }

    }
}

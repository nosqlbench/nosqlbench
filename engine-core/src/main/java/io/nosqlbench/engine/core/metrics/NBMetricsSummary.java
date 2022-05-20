package io.nosqlbench.engine.core.metrics;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.codahale.metrics.*;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class NBMetricsSummary {
    public static void summarize(StringBuilder sb, String name, Metric metric) {
        sb.append(String.format("%-40s", name));

        if (metric instanceof Counting) {
            sb.append(" count=").append(((Counting) metric).getCount());
        }

        if (metric instanceof Gauge) {
            sb.append(" value=").append(((Gauge<?>) metric).getValue());
        }

        sb.append("\n");

        return;

//        if (metric instanceof Gauge) {
//            sb.append(" value=").append(((Gauge<?>) metric).getValue());
//        }
//        if (metric instanceof Metered) {
//            sb.append("\n rate_1m=").append(((Metered) metric).getOneMinuteRate())
//                .append(", rate_5m=").append(((Metered) metric).getFiveMinuteRate())
//                .append(", rate_15m=").append(((Metered) metric).getFifteenMinuteRate())
//                .append(", rate_mean=").append(((Metered) metric).getMeanRate());
//        }
//        if (metric instanceof Sampling) {
//            Snapshot s = ((Sampling) metric).getSnapshot();
//
//            sb.append(String.format(
//                    "\n %d %f %f %f %f %f %f %d",
//                    s.getMin(),
//                    s.getValue(0.5d),
//                    s.getValue(0.75d),
//                    s.getValue(0.95d),
//                    s.getValue(0.98d),
//                    s.getValue(0.99d),
//                    s.getValue(0.999d),
//                    s.getMax()))
//                .append(String.format(
//                    "\n %s %s %s %s %s %s %s %s",
//                    fmt(s.getMin()),
//                    fmt(s.getValue(0.5d)),
//                    fmt(s.getValue(0.75d)),
//                    fmt(s.getValue(0.95d)),
//                    fmt(s.getValue(0.98d)),
//                    fmt(s.getValue(0.99d)),
//                    fmt(s.getValue(0.999d)),
//                    fmt(s.getMax())));
//        }
//        sb.append("\n");
    }

    public static void summarize(StringBuilder sb, MetricRegistry registry) {
        registry.getMetrics().forEach((k, v) -> {
            summarize(sb, k, v);
        });
    }

    private static String fmt(Number n) {

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMaximumFractionDigits(3);
        fmt.setGroupingUsed(false);
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(n);
    }

}

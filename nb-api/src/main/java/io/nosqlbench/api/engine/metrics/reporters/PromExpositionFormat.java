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

package io.nosqlbench.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.testutils.Perf;

import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * Format NBMetrics according to the prometheus exposition format.
 *
 * @see <a
 *     href="https://github.com/prometheus/docs/blob/main/content/docs/instrumenting/exposition_formats.md">prometheus
 *     exposition format</a>
 */

public enum PromExpositionFormat {
    ;

    public static String format(Clock clock, Metric... metrics) {
        return format(clock, new StringBuilder(), metrics).toString();
    }

    /**
     * @param clock
     *     The clock to use for assigning an observation time to each metric value.
     * @param builder
     *     A string builder to append to
     * @param metrics
     *     zero or more metric which need to be formatted
     * @return A string representation of the metrics in prometheus exposition format
     */
    public static StringBuilder format(Clock clock, StringBuilder builder, Metric... metrics) {
        StringBuilder buffer = null != builder ? builder : new StringBuilder();
        Instant instant = clock.instant();

        for (Metric metric : metrics) {
            NBLabels labels;

            if (metric instanceof NBLabeledElement labeled) {
                labels = labeled.getLabels();
            } else {
                throw new RuntimeException(
                    "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
                );
            }
//            String metricNameAndLabels = labels.linearize("name");
            long epochMillis = instant.toEpochMilli();

            if (metric instanceof Counting counting) {
                buffer.append("# TYPE ")
                    .append(labels.modifyValue("name", n -> n+"_total").only("name")).append(" counter\n");

                long count = counting.getCount();
                buffer
                    .append(labels.modifyValue("name", n -> n+"_total"))
                    .append(' ')
                    .append(count)
                    .append(' ')
                    .append(epochMillis)
                    .append('\n');
            }
            if (metric instanceof Sampling sampling) {
                // Use the summary form
                buffer.append("# TYPE ").append(labels.only("name")).append(" summary\n");
                Snapshot snapshot = sampling.getSnapshot();
                for (double quantile : new double[]{0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999}) {
                    double value = snapshot.getValue(quantile);
                    buffer
                        .append(labels.and("quantile", String.valueOf(quantile)))
                        .append(' ')
                        .append(value)
                        .append('\n');
                }
                double snapshotCount =snapshot.size();
                buffer.append(labels.modifyValue("name",n->n+"_count"))
                    .append(' ')
                    .append(snapshotCount)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.only("name")).append("_max").append(" gauge\n");
                long maxValue = snapshot.getMax();
                buffer.append(labels.modifyValue("name",n->n+"_max"))
                    .append(' ')
                    .append(maxValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.only("name")).append("_min").append(" gauge\n");
                long minValue = snapshot.getMin();
                buffer.append(labels.modifyValue("name",n->n+"_min"))
                    .append(' ')
                    .append(minValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.only("name")).append("_mean").append(" gauge\n");
                double meanValue = snapshot.getMean();
                buffer.append(labels.modifyValue("name",n->n+"_mean"))
                    .append(' ')
                    .append(meanValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.only("name")).append("_stdev").append(" gauge\n");
                double stdDev = snapshot.getStdDev();
                buffer.append(labels.modifyValue("name",n->n+"_stdev"))
                    .append(' ')
                    .append(stdDev)
                    .append('\n');

            }
            if (metric instanceof Gauge gauge) {
                buffer.append("# TYPE ").append(labels.only("name")).append(" gauge\n");
                Object value = gauge.getValue();
                if (value instanceof Number number) {
                    double doubleValue = number.doubleValue();
                    buffer.append(labels)
                        .append(' ')
                        .append(doubleValue)
                        .append('\n');
                } else if (value instanceof CharSequence sequence) {
                    String stringValue = sequence.toString();
                    buffer.append(labels)
                        .append(' ')
                        .append(stringValue)
                        .append('\n');
                } else if (value instanceof String stringValue) buffer.append(labels)
                    .append(' ')
                    .append(stringValue)
                    .append('\n');
                else {
                    throw new RuntimeException(
                        "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
                    );
                }
            }
            if (metric instanceof Metered meter) {
                buffer.append("# TYPE ").append(labels.only("name")).append("_1mRate").append(" gauge\n");
                double oneMinuteRate = meter.getOneMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_1mRate"))
                    .append(' ')
                    .append(oneMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.only("name")).append("_5mRate").append(" gauge\n");
                double fiveMinuteRate = meter.getFiveMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_5mRate"))
                    .append(' ')
                    .append(fiveMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.only("name")).append("_15mRate").append(" gauge\n");
                double fifteenMinuteRate = meter.getFifteenMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_15mRate"))
                    .append(' ')
                    .append(fifteenMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.only("name")).append("_meanRate").append(" gauge\n");
                double meanRate = meter.getMeanRate();
                buffer.append(labels.modifyValue("name",n->n+"_meanRate"))
                    .append(' ')
                    .append(meanRate)
                    .append('\n');

            }

        }

        return buffer;


    }

    public static String labels(Map<String, String> labels, String... additional) {
        StringBuilder sb = new StringBuilder("{");
        for (String labelName : labels.keySet()) {
            if ("name".equals(labelName)) {
                continue;
            }
            sb.append(labelName)
                .append("=\"")
                .append(labels.get(labelName))
                .append('"')
                .append(',');
        }
        sb.setLength(sb.length() - 1);

//        if (additional.length > 0) {
            for (int i = 0; i < additional.length; i += 2) {
                sb.append(',')
                    .append(additional[i])
                    .append("=\"")
                    .append(additional[i + 1])
                    .append('"');
            }
//        }

        sb.append('}');
        return sb.toString();
    }

    private static void writeEscapedHelp(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

}

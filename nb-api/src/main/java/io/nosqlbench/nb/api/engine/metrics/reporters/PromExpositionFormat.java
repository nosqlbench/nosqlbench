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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Format NBMetrics according to the prometheus exposition format.
 *
 * @see <a
 *     href="https://github.com/prometheus/docs/blob/main/content/docs/instrumenting/exposition_formats.md">prometheus
 *     exposition format</a>
 */

public class PromExpositionFormat {

    private final static Logger logger = LogManager.getLogger("METRICS");
    public static String format(final Clock clock, final Metric... metrics) {
        return PromExpositionFormat.format(clock, new StringBuilder(), metrics).toString();
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
    public static StringBuilder format(final Clock clock, final StringBuilder builder, final Object... metrics) {
        final StringBuilder buffer = (null != builder) ? builder : new StringBuilder();
        final Instant instant = clock.instant();

        for (final Object metric : metrics) {
            NBLabels labels = null;

            if (metric instanceof final NBLabeledElement labeled) labels = labeled.getLabels();
            else throw new RuntimeException(
                "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
            );
            final long epochMillis = instant.toEpochMilli();

            if (metric instanceof final Counting counting) {
                buffer.append("# TYPE ")
                    .append(labels.modifyValue("name", n -> n+"_total").valueOf("name")).append(" counter\n");

                final long count = counting.getCount();
                buffer
                    .append(labels.modifyValue("name", n -> n+"_total").linearize("name"))
                    .append(' ')
                    .append(count)
                    .append(' ')
                    .append(epochMillis)
                    .append('\n');
            }
            if (metric instanceof final Sampling sampling) {
                // Use the summary form
                buffer.append("# TYPE ").append(labels.valueOf("name")).append(" histogram\n");
                final Snapshot snapshot = sampling.getSnapshot();
                for (final double quantile : new double[]{0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999}) {
                    final double value = snapshot.getValue(quantile);
                    buffer
                        .append(labels.modifyValue("name",n -> n+"_bucket").and("le", String.valueOf(quantile)).linearize("name"))
//                        .append(labels.andTypes("quantile", String.valueOf(quantile)).linearize("name"))
                        .append(' ')
                        .append(value)
                        .append('\n');
                }
                buffer.append(labels.modifyValue("name",n->n+"_bucket").and("le","+Inf").linearize("name"))
                    .append(' ')
                    .append(snapshot.getMax())
                    .append('\n');
                final double snapshotCount =snapshot.size();
                buffer.append(labels.modifyValue("name",n->n+"_count").linearize("name"))
                    .append(' ')
                    .append(snapshotCount)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.valueOf("name")).append("_max").append(" gauge\n");
                final long maxValue = snapshot.getMax();
                buffer.append(labels.modifyValue("name",n->n+"_max").linearize("name"))
                    .append(' ')
                    .append(maxValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_min").valueOf("name")).append(" gauge\n");
                final long minValue = snapshot.getMin();
                buffer.append(labels.modifyValue("name",n->n+"_min").linearize("name"))
                    .append(' ')
                    .append(minValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_mean").valueOf("name")).append(" gauge\n");
                final double meanValue = snapshot.getMean();
                buffer.append(labels.modifyValue("name",n->n+"_mean").linearize("name"))
                    .append(' ')
                    .append(meanValue)
                    .append('\n');
                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_stdev").valueOf("name")).append(" gauge\n");
                final double stdDev = snapshot.getStdDev();
                buffer.append(labels.modifyValue("name",n->n+"_stdev").linearize("name"))
                    .append(' ')
                    .append(stdDev)
                    .append('\n');

            }
            if (metric instanceof final Gauge gauge) {
                buffer.append("# TYPE ").append(labels.valueOf("name")).append(" gauge\n");
                final Object value = gauge.getValue();
                if (value instanceof final Number number) {
                    final double doubleValue = number.doubleValue();
                    buffer.append(labels.linearize("name"))
                        .append(' ')
                        .append(doubleValue)
                        .append('\n');
                } else if (value instanceof final CharSequence sequence) {
                    final String stringValue = sequence.toString();
                    buffer.append(labels.linearize("name"))
                        .append(' ')
                        .append(stringValue)
                        .append('\n');
                } else if (value instanceof final String stringValue) {
                    buffer.append(labels.linearize("name"))
                        .append(' ')
                        .append(stringValue)
                        .append('\n');
                } else throw new RuntimeException(
                        "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
                    );
            }
            if (metric instanceof final Metered meter) {
                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_1mRate").valueOf("name")).append(" gauge\n");
                final double oneMinuteRate = meter.getOneMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_1mRate").linearize("name"))
                    .append(' ')
                    .append(oneMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_5mRate").valueOf("name")).append(" gauge\n");
                final double fiveMinuteRate = meter.getFiveMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_5mRate").linearize("name"))
                    .append(' ')
                    .append(fiveMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_15mRate").valueOf("name")).append(" gauge\n");
                final double fifteenMinuteRate = meter.getFifteenMinuteRate();
                buffer.append(labels.modifyValue("name",n->n+"_15mRate").linearize("name"))
                    .append(' ')
                    .append(fifteenMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(labels.modifyValue("name",n->n+"_meanRate").valueOf("name")).append(" gauge\n");
                final double meanRate = meter.getMeanRate();
                buffer.append(labels.modifyValue("name",n->n+"_meanRate").linearize("name"))
                    .append(' ')
                    .append(meanRate)
                    .append('\n');

            }

        }

        return buffer;
    }

    public static String labels(final Map<String, String> labels, final String... additional) {
        final StringBuilder sb = new StringBuilder("{");
        for (final String labelName : labels.keySet()) {
            if ("name".equals(labelName)) continue;
            sb.append(labelName)
                .append("=\"")
                .append(labels.get(labelName))
                .append('"')
                .append(',');
        }
        sb.setLength(sb.length() - 1);

//        if (additional.length > 0) {
            for (int i = 0; i < additional.length; i += 2)
                sb.append(',')
                    .append(additional[i])
                    .append("=\"")
                    .append(additional[i + 1])
                    .append('"');
//        }

        sb.append('}');
        return sb.toString();
    }

    private static void writeEscapedHelp(final Writer writer, final String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
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

    private static String sanitize(String word) {
        String sanitized = word;
        sanitized = sanitized.replaceAll("\\..+$", "");
        sanitized = sanitized.replaceAll("-","_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]+", "");

        if (!word.equals(sanitized)) {
            logger.warn("The identifier or value '" + word + "' was sanitized to '" + sanitized + "' to be compatible with monitoring systems. You should probably change this to make diagnostics easier.");
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            StringBuilder stb = new StringBuilder();
            for (StackTraceElement element : elements) {
                stb.append("\tat ").append(element).append("\n");
            }
            logger.warn("stacktrace: " + stb.toString());

        }
        return sanitized;
    }

}

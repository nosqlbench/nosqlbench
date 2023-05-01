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

public class PromExpositionFormat {
    ;

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
    public static StringBuilder format(final Clock clock, final StringBuilder builder, final Metric... metrics) {
        final StringBuilder buffer = (null != builder) ? builder : new StringBuilder();
        final Instant instant = clock.instant();

        for (final Metric metric : metrics) {
            final Map<String, String> labels;

            if (metric instanceof NBLabeledElement labeled) labels = labeled.getLabels();
            else throw new RuntimeException(
                "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
            );
            String rawName = labels.get("name");
            final long epochMillis = instant.toEpochMilli();

            if (metric instanceof Counting counting) {
                final String basename = rawName + "_total";
                buffer.append("# TYPE ").append(basename).append(" counter\n");

                final long count = counting.getCount();
                buffer
                    .append(basename)
                    .append(PromExpositionFormat.labels(labels))
                    .append(' ')
                    .append(count)
                    .append(' ')
                    .append(epochMillis)
                    .append('\n');
            }
            if (metric instanceof Sampling sampling) {
                // Use the summary form
                buffer.append("# TYPE ").append(rawName).append(" summary\n");
                final Snapshot snapshot = sampling.getSnapshot();
                for (final double quantile : new double[]{0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999}) {
                    final double value = snapshot.getValue(quantile);
                    buffer.append(rawName)
                        .append(PromExpositionFormat.labels(labels, "quantile", String.valueOf(quantile)))
                        .append(' ')
                        .append(value)
                        .append('\n');
                }
                final double snapshotCount =snapshot.size();
                buffer.append(rawName)
                    .append("_count")
                    .append(' ')
                    .append(snapshotCount)
                    .append('\n');
                buffer.append("# TYPE ").append(rawName).append("_max").append(" gauge\n");
                final long maxValue = snapshot.getMax();
                buffer.append(rawName)
                    .append("_max")
                    .append(' ')
                    .append(maxValue)
                    .append('\n');
                buffer.append("# TYPE ").append(rawName).append("_min").append(" gauge\n");
                final long minValue = snapshot.getMin();
                buffer.append(rawName)
                    .append("_min")
                    .append(' ')
                    .append(minValue)
                    .append('\n');
                buffer.append("# TYPE ").append(rawName).append("_mean").append(" gauge\n");
                final double meanValue = snapshot.getMean();
                buffer.append(rawName)
                    .append("_mean")
                    .append(' ')
                    .append(meanValue)
                    .append('\n');
                buffer.append("# TYPE ").append(rawName).append("_stdev").append(" gauge\n");
                final double stdDev = snapshot.getStdDev();
                buffer.append(rawName)
                    .append("_stdev")
                    .append(' ')
                    .append(stdDev)
                    .append('\n');

            }
            if (metric instanceof Gauge gauge) {
                buffer.append("# TYPE ").append(rawName).append(" gauge\n");
                final Object value = gauge.getValue();
                if (value instanceof Number number) {
                    final double doubleValue = number.doubleValue();
                    buffer.append(rawName)
                        .append(' ')
                        .append(doubleValue)
                        .append('\n');
                } else if (value instanceof CharSequence sequence) {
                    final String stringValue = sequence.toString();
                    buffer.append(rawName)
                        .append(' ')
                        .append(stringValue)
                        .append('\n');
                } else if (value instanceof String stringValue) {
                    buffer.append(rawName)
                        .append(' ')
                        .append(stringValue)
                        .append('\n');
                } else throw new RuntimeException(
                    "Unknown label set for metric type '" + metric.getClass().getCanonicalName() + '\''
                );
            }
            if (metric instanceof Metered meter) {
                buffer.append("# TYPE ").append(rawName).append("_1mRate").append(" gauge\n");
                final double oneMinuteRate = meter.getOneMinuteRate();
                buffer.append(rawName)
                    .append("_1mRate")
                    .append(' ')
                    .append(oneMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(rawName).append("_5mRate").append(" gauge\n");
                final double fiveMinuteRate = meter.getFiveMinuteRate();
                buffer.append(rawName)
                    .append("_5mRate")
                    .append(' ')
                    .append(fiveMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(rawName).append("_15mRate").append(" gauge\n");
                final double fifteenMinuteRate = meter.getFifteenMinuteRate();
                buffer.append(rawName)
                    .append("_15mRate")
                    .append(' ')
                    .append(fifteenMinuteRate)
                    .append('\n');

                buffer.append("# TYPE ").append(rawName).append("_meanRate").append(" gauge\n");
                final double meanRate = meter.getMeanRate();
                buffer.append(rawName)
                    .append("_meanRate")
                    .append(' ')
                    .append(meanRate)
                    .append('\n');

            }

        }

        return buffer;


//        if (metricFamilySamples.type == Collector.Type.COUNTER) {
//            writer.write("_total");
//        }
//        if (metricFamilySamples.type == Collector.Type.INFO) {
//            writer.write("_info");
//        }
//        writer.write(' ');
//        writeEscapedHelp(writer, help);
//        writer.write('\n');
//
//        writer.write("# TYPE ");
//        writer.write(basename);
//        if (metricFamilySamples.type == Collector.Type.COUNTER) {
//            writer.write("_total");
//        }
//        if (metricFamilySamples.type == Collector.Type.INFO) {
//            writer.write("_info");
//        }
//        writer.write(' ');
//        writer.write(typeString(metricFamilySamples.type));
//        writer.write('\n');
//
//        String createdName = basename + "_created";
//        String gcountName = basename + "_gcount";
//        String gsumName = basename + "_gsum";
//        for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
//            /* OpenMetrics specific sample, put in a gauge at the end. */
//            if (sample.name.equals(createdName)
//                || sample.name.equals(gcountName)
//                || sample.name.equals(gsumName)) {
//                Collector.MetricFamilySamples omFamily = omFamilies.get(sample.name);
//                if (omFamily == null) {
//                    omFamily = new Collector.MetricFamilySamples(sample.name, Collector.Type.GAUGE, metricFamilySamples.help, new ArrayList<Collector.MetricFamilySamples.Sample>());
//                    omFamilies.put(sample.name, omFamily);
//                }
//                omFamily.samples.add(sample);
//                continue;
//            }
//            writer.write(sample.name);
//            if (sample.labelNames.size() > 0) {
//                writer.write('{');
//                for (int i = 0; i < sample.labelNames.size(); ++i) {
//                    writer.write(sample.labelNames.get(i));
//                    writer.write("=\"");
//                    writeEscapedLabelValue(writer, sample.labelValues.get(i));
//                    writer.write("\",");
//                }
//                writer.write('}');
//            }
//            writer.write(' ');
//            writer.write(Collector.doubleToGoString(sample.value));
//            if (sample.timestampMs != null) {
//                writer.write(' ');
//                writer.write(sample.timestampMs.toString());
//            }
//            writer.write('\n');
//        }
        // Write out any OM-specific samples.
//        if (!omFamilies.isEmpty()) {
//            write004(writer, Collections.enumeration(omFamilies.values()));
//        }
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

}

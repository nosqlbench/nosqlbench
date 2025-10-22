/*
 * Copyright (c) nosqlbench
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

import com.codahale.metrics.Gauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;
import io.nosqlbench.nb.api.engine.metrics.reporters.PromExpositionFormat;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class PromExpositionFormatTest {

    private final Clock nowclock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    @Test
    public void testLabelFormat() {
        assertThat(
            NBLabels.forMap(Map.of("name","namefoo","property2","value2")).linearize("name")
        ).isEqualTo("""
            namefoo{property2="value2"}""");
    }
    @Test
    public void testCounterFormat() {
        NBMetricCounter counter = new NBMetricCounter(
            NBLabels.forKV("name","counter_test_2342", "origin","mars"),
            "test counter format",
            MetricCategory.Verification
        );
        counter.inc(23423L);

        String buffer = PromExpositionFormat.format(nowclock, counter);
        long epoch = nowclock.instant().toEpochMilli();
        assertThat(buffer).contains("# TYPE counter_test_2342_total counter");
        assertThat(buffer).contains("# HELP counter_test_2342_total test counter format");
        assertThat(buffer).contains("counter_test_2342_total{origin=\"mars\"} 23423 " + epoch);
    }

    @Test
    public void testHistogramFormat() {

        DeltaHdrHistogramReservoir hdr = new DeltaHdrHistogramReservoir(NBLabels.forKV("name","mynameismud","label3","value3"),3);

        for (long i = 0; 1000 > i; i++) {
            hdr.update(i * 37L);
        }
        NBMetricHistogram nbHistogram = new NBMetricHistogram(
            NBLabels.forKV("name","mynameismud","label3", "value3"),
            hdr,
            "test histogram format",
            MetricCategory.Verification
            );
        String formatted = PromExpositionFormat.format(nowclock, nbHistogram);

        long epoch = nowclock.instant().toEpochMilli();
        assertThat(formatted).contains("# TYPE mynameismud summary");
        assertThat(formatted).contains("# HELP mynameismud test histogram format");
        assertThat(formatted).contains("# CATEGORIES mynameismud Verification");
        assertThat(formatted).contains("mynameismud{label3=\"value3\",quantile=\"0.5\"}");
        assertThat(formatted).contains("mynameismud_count{label3=\"value3\"} 1000 " + epoch);
        assertThat(formatted).contains("mynameismud_sum{label3=\"value3\"}");
    }

    @Test
    public void testTimerFormat() {

        DeltaHdrHistogramReservoir hdr = new DeltaHdrHistogramReservoir(NBLabels.forKV("name","monsieurmarius","label4","value4"),3);
        NBMetricTimer nbMetricTimer = new NBMetricTimer(
            NBLabels.forKV("name","monsieurmarius","label4", "value4"),
            hdr,
            "test timer format",
            MetricCategory.Verification
        );
        for (long i = 0; 1000 > i; i++)
            nbMetricTimer.update(i * 37L, TimeUnit.NANOSECONDS);

        String formatted = PromExpositionFormat.format(nowclock, nbMetricTimer);
        long epoch = nowclock.instant().toEpochMilli();

        assertThat(formatted).contains("# TYPE monsieurmarius summary");
        assertThat(formatted).contains("# HELP monsieurmarius test timer format");
        assertThat(formatted).contains("# CATEGORIES monsieurmarius Verification");
        assertThat(formatted).contains("monsieurmarius{label4=\"value4\",quantile=\"0.5\"}");
        assertThat(formatted).contains("monsieurmarius_count{label4=\"value4\"} 1000 " + epoch);
        assertThat(formatted).contains("monsieurmarius_sum{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_min{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_max{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_mean_rate{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_m1_rate{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_m5_rate{label4=\"value4\"}");
        assertThat(formatted).contains("monsieurmarius_m15_rate{label4=\"value4\"}");
    }

    @Test
    public void testMeterFormat() {
        NBMetricMeter nbMetricMeter = new NBMetricMeter(
            NBLabels.forKV("name","eponine","label5", "value5"),
            "test meter format",
            MetricCategory.Verification
        );
        String formatted = PromExpositionFormat.format(nowclock, nbMetricMeter);
        long epoch = nowclock.instant().toEpochMilli();

        assertThat(formatted).contains("# TYPE eponine_total counter");
        assertThat(formatted).contains("# HELP eponine_total test meter format");
        assertThat(formatted).contains("# CATEGORIES eponine_total Verification");
        assertThat(formatted).contains("eponine_total{label5=\"value5\"} 0 " + epoch);
        assertThat(formatted).contains("eponine_mean_rate{label5=\"value5\"} 0 " + epoch);
        assertThat(formatted).contains("eponine_m1_rate{label5=\"value5\"} 0 " + epoch);
        assertThat(formatted).contains("eponine_m5_rate{label5=\"value5\"} 0 " + epoch);
        assertThat(formatted).contains("eponine_m15_rate{label5=\"value5\"} 0 " + epoch);
    }

    @Test
    public void testGaugeFormat() {
        Gauge cosetteGauge = () -> 1500d;
        NBMetricGauge nbMetricGauge = new NBMetricGaugeWrapper(
            NBLabels.forKV("name","cosette","label6", "value6"),
            cosetteGauge,
            "test gauge format",
            MetricCategory.Verification
        );
        String formatted = PromExpositionFormat.format(nowclock, nbMetricGauge);
        long epoch = nowclock.instant().toEpochMilli();

        assertThat(formatted).contains("# TYPE cosette gauge");
        assertThat(formatted).contains("# HELP cosette test gauge format");
        assertThat(formatted).contains("# CATEGORIES cosette Verification");
        assertThat(formatted).contains("cosette{label6=\"value6\"} 1500 " + epoch);

        Gauge cosetteGauge2 = () -> 2000.0d;
        NBMetricGauge nbMetricGauge2 = new NBMetricGaugeWrapper(
            NBLabels.forKV("name","cosette2","label7", "value7"),
            cosetteGauge2,
            "test gauge format 2",
            MetricCategory.Verification
        );
        String formatted2 = PromExpositionFormat.format(nowclock, nbMetricGauge2);
        assertThat(formatted2).contains("# TYPE cosette2 gauge");
        assertThat(formatted2).contains("# HELP cosette2 test gauge format 2");
        assertThat(formatted2).contains("# CATEGORIES cosette2 Verification");
        assertThat(formatted2).contains("cosette2{label7=\"value7\"} 2000 " + epoch);

    }
}

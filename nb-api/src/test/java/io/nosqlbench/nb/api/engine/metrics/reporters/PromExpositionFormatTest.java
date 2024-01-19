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

import com.codahale.metrics.Counter;
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
import java.util.regex.Pattern;

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
        Counter counter = new NBMetricCounter(
            NBLabels.forKV("name","counter_test_2342", "origin","mars"),
            "test counter format",
            MetricCategory.Verification
        );
        counter.inc(23423L);

        String buffer = PromExpositionFormat.format(nowclock, counter);
        assertThat(buffer).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test counter format
            # TYPE counter_test_2342_total counter
            counter_test_2342_total\\{origin="mars"} \\d+ \\d+
            """));
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

        assertThat(formatted).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test histogram format
            # TYPE mynameismud_total counter
            mynameismud_total\\{label3="value3"} 0 \\d+
            # TYPE mynameismud histogram
            mynameismud_bucket\\{label3="value3",le="0.5"} 18463.0
            mynameismud_bucket\\{label3="value3",le="0.75"} 27727.0
            mynameismud_bucket\\{label3="value3",le="0.9"} 33279.0
            mynameismud_bucket\\{label3="value3",le="0.95"} 35135.0
            mynameismud_bucket\\{label3="value3",le="0.98"} 36223.0
            mynameismud_bucket\\{label3="value3",le="0.99"} 36607.0
            mynameismud_bucket\\{label3="value3",le="0.999"} 36927.0
            mynameismud_bucket\\{label3="value3",le="\\+Inf"} 36991
            mynameismud_count\\{label3="value3"} 1000.0
            # TYPE mynameismud_max gauge
            mynameismud_max\\{label3="value3"} 36991
            # TYPE mynameismud_min gauge
            mynameismud_min\\{label3="value3"} 0
            # TYPE mynameismud_mean gauge
            mynameismud_mean\\{label3="value3"} 18481.975
            # TYPE mynameismud_stdev gauge
            mynameismud_stdev\\{label3="value3"} 10681.018083421426
            """));
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

        assertThat(formatted).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test timer format
            # TYPE monsieurmarius_total counter
            monsieurmarius_total\\{label4="value4"} 1000 \\d+
            # TYPE monsieurmarius histogram
            monsieurmarius_bucket\\{label4="value4",le="0.5"} 18463.0
            monsieurmarius_bucket\\{label4="value4",le="0.75"} 27727.0
            monsieurmarius_bucket\\{label4="value4",le="0.9"} 33279.0
            monsieurmarius_bucket\\{label4="value4",le="0.95"} 35135.0
            monsieurmarius_bucket\\{label4="value4",le="0.98"} 36223.0
            monsieurmarius_bucket\\{label4="value4",le="0.99"} 36607.0
            monsieurmarius_bucket\\{label4="value4",le="0.999"} 36927.0
            monsieurmarius_bucket\\{label4="value4",le="\\+Inf"} 36991
            monsieurmarius_count\\{label4="value4"} 1000.0
            # TYPE monsieurmarius_max gauge
            monsieurmarius_max\\{label4="value4"} 36991
            # TYPE monsieurmarius_min gauge
            monsieurmarius_min\\{label4="value4"} 0
            # TYPE monsieurmarius_mean gauge
            monsieurmarius_mean\\{label4="value4"} 18481.975
            # TYPE monsieurmarius_stdev gauge
            monsieurmarius_stdev\\{label4="value4"} \\d+\\.\\d+
            # TYPE monsieurmarius_1mRate gauge
            monsieurmarius_1mRate\\{label4="value4"} 0.0
            # TYPE monsieurmarius_5mRate gauge
            monsieurmarius_5mRate\\{label4="value4"} 0.0
            # TYPE monsieurmarius_15mRate gauge
            monsieurmarius_15mRate\\{label4="value4"} 0.0
            # TYPE monsieurmarius_meanRate gauge
            monsieurmarius_meanRate\\{label4="value4"} \\d+\\.\\d+
            """));
    }

    @Test
    public void testMeterFormat() {
        NBMetricMeter nbMetricMeter = new NBMetricMeter(
            NBLabels.forKV("name","eponine","label5", "value5"),
            "test meter format",
            MetricCategory.Verification
        );
        String formatted = PromExpositionFormat.format(nowclock, nbMetricMeter);

        assertThat(formatted).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test meter format
            # TYPE eponine_total counter
            eponine_total\\{label5="value5"} 0 \\d+
            # TYPE eponine_1mRate gauge
            eponine_1mRate\\{label5="value5"} 0.0
            # TYPE eponine_5mRate gauge
            eponine_5mRate\\{label5="value5"} 0.0
            # TYPE eponine_15mRate gauge
            eponine_15mRate\\{label5="value5"} 0.0
            # TYPE eponine_meanRate gauge
            eponine_meanRate\\{label5="value5"} 0.0
            """));
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

        assertThat(formatted).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test gauge format
            # TYPE cosette gauge
            cosette\\{label6="value6"} 1500.0
            """));

        Gauge cosetteGauge2 = () -> 2000.0d;
        NBMetricGauge nbMetricGauge2 = new NBMetricGaugeWrapper(
            NBLabels.forKV("name","cosette2","label7", "value7"),
            cosetteGauge2,
            "test gauge format 2",
            MetricCategory.Verification
        );
        String formatted2 = PromExpositionFormat.format(nowclock, nbMetricGauge2);

        assertThat(formatted2).matches(Pattern.compile("""
            # CATEGORIES: Verification
            # DESCRIPTION: test gauge format 2
            # TYPE cosette2 gauge
            cosette2\\{label7="value7"} 2000.0
            """));

    }
}

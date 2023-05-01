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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.engine.metrics.instruments.*;
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
            PromExpositionFormat.labels(Map.of("name","namefoo","property2","value2"))
        ).isEqualTo("""
            {property2="value2"}""");
    }
    @Test
    public void testCounterFormat() {
        final Counter counter = new NBMetricCounter(Map.of("name","counter_test_2342", "origin","mars"));
        counter.inc(23423L);

        final String buffer = PromExpositionFormat.format(this.nowclock, counter);
        assertThat(buffer).matches(Pattern.compile("""
            # TYPE counter_test_2342_total counter
            counter_test_2342_total\\{origin="mars"} \\d+ \\d+
            """));
    }

    @Test
    public void testHistogramFormat() {

        final DeltaHdrHistogramReservoir hdr = new DeltaHdrHistogramReservoir(Map.of("label3","value3"),3);

        for (long i = 0; 1000 > i; i++) hdr.update(i * 37L);
        final NBMetricHistogram nbHistogram = new NBMetricHistogram(Map.of("name","mynameismud","label3", "value3"), hdr);
        final String formatted = PromExpositionFormat.format(this.nowclock, nbHistogram);

        assertThat(formatted).matches(Pattern.compile("""
            # TYPE mynameismud_total counter
            mynameismud_total\\{label3="value3"} 0 \\d+
            # TYPE mynameismud summary
            mynameismud\\{label3="value3",quantile="0.5"} 18463.0
            mynameismud\\{label3="value3",quantile="0.75"} 27727.0
            mynameismud\\{label3="value3",quantile="0.9"} 33279.0
            mynameismud\\{label3="value3",quantile="0.95"} 35135.0
            mynameismud\\{label3="value3",quantile="0.98"} 36223.0
            mynameismud\\{label3="value3",quantile="0.99"} 36607.0
            mynameismud\\{label3="value3",quantile="0.999"} 36927.0
            mynameismud_count 1000.0
            # TYPE mynameismud_max gauge
            mynameismud_max 36991
            # TYPE mynameismud_min gauge
            mynameismud_min 0
            # TYPE mynameismud_mean gauge
            mynameismud_mean 18481.975
            # TYPE mynameismud_stdev gauge
            mynameismud_stdev 10681.018083421426
            """));
    }

    @Test
    public void testTimerFormat() {

        final DeltaHdrHistogramReservoir hdr = new DeltaHdrHistogramReservoir(Map.of("label4","value4"),3);
        final NBMetricTimer nbMetricTimer = new NBMetricTimer(Map.of("name","monsieurmarius","label4", "value4"), hdr);
        for (long i = 0; 1000 > i; i++)
        {
            nbMetricTimer.update(i*37L, TimeUnit.NANOSECONDS);
        }

        final String formatted = PromExpositionFormat.format(this.nowclock, nbMetricTimer);

        assertThat(formatted).matches(Pattern.compile("""
            # TYPE monsieurmarius_total counter
            monsieurmarius_total\\{label4="value4"} 1000 \\d+
            # TYPE monsieurmarius summary
            monsieurmarius\\{label4="value4",quantile="0.5"} 18463.0
            monsieurmarius\\{label4="value4",quantile="0.75"} 27727.0
            monsieurmarius\\{label4="value4",quantile="0.9"} 33279.0
            monsieurmarius\\{label4="value4",quantile="0.95"} 35135.0
            monsieurmarius\\{label4="value4",quantile="0.98"} 36223.0
            monsieurmarius\\{label4="value4",quantile="0.99"} 36607.0
            monsieurmarius\\{label4="value4",quantile="0.999"} 36927.0
            monsieurmarius_count 1000.0
            # TYPE monsieurmarius_max gauge
            monsieurmarius_max 36991
            # TYPE monsieurmarius_min gauge
            monsieurmarius_min 0
            # TYPE monsieurmarius_mean gauge
            monsieurmarius_mean 18481.975
            # TYPE monsieurmarius_stdev gauge
            monsieurmarius_stdev \\d+\\.\\d+
            # TYPE monsieurmarius_1mRate gauge
            monsieurmarius_1mRate 0.0
            # TYPE monsieurmarius_5mRate gauge
            monsieurmarius_5mRate 0.0
            # TYPE monsieurmarius_15mRate gauge
            monsieurmarius_15mRate 0.0
            # TYPE monsieurmarius_meanRate gauge
            monsieurmarius_meanRate \\d+\\.\\d+
            """));
    }

    @Test
    public void testMeterFormat() {
        final NBMetricMeter nbMetricMeter = new NBMetricMeter(Map.of("name","eponine","label5", "value5"));
        final String formatted = PromExpositionFormat.format(this.nowclock, nbMetricMeter);

        assertThat(formatted).matches(Pattern.compile("""
            # TYPE eponine_total counter
            eponine_total\\{label5="value5"} 0 \\d+
            # TYPE eponine_1mRate gauge
            eponine_1mRate 0.0
            # TYPE eponine_5mRate gauge
            eponine_5mRate 0.0
            # TYPE eponine_15mRate gauge
            eponine_15mRate 0.0
            # TYPE eponine_meanRate gauge
            eponine_meanRate 0.0
            """));
    }

    @Test
    public void testGaugeFormat() {
        final Gauge cosetteGauge = () -> 1500;
        final NBMetricGauge nbMetricGauge = new NBMetricGauge(Map.of("name","cosette","label6", "value6"), cosetteGauge);
        final String formatted = PromExpositionFormat.format(this.nowclock, nbMetricGauge);

        assertThat(formatted).matches(Pattern.compile("""
            # TYPE cosette gauge
            cosette 1500.0
            """));

        final Gauge cosetteGauge2 = () -> new String("2000.0");
        final NBMetricGauge nbMetricGauge2 = new NBMetricGauge(Map.of("name","cosette2","label7", "value7"), cosetteGauge2);
        final String formatted2 = PromExpositionFormat.format(this.nowclock, nbMetricGauge2);

        assertThat(formatted2).matches(Pattern.compile("""
            # TYPE cosette2 gauge
            cosette2 2000.0
            """));

        final int number = 3000;
        CharSequence charSequence = Integer.toString(number);
        final Gauge cosetteGauge3 = () -> charSequence;
        final NBMetricGauge nbMetricGauge3 = new NBMetricGauge(Map.of("name","cosette3","label8", "value8"), cosetteGauge3);
        final String formatted3 = PromExpositionFormat.format(this.nowclock, nbMetricGauge3);

        assertThat(formatted3).matches(Pattern.compile("""
            # TYPE cosette3 gauge
            cosette3 3000
            """));
    }
}

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
import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricHistogram;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
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
            """));
    }

}

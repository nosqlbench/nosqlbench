/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityMetricsTest {

    @Test
    public void testMountSubRegistry() throws Exception {
        MetricRegistry r1 = new MetricRegistry();
        r1.counter("counter1");

        int extant = ActivityMetrics.getMetricRegistry().getMetrics().size();

        ActivityMetrics.mountSubRegistry("aprefixhere.",r1);
        Map<String, Metric> metrics = ActivityMetrics.getMetricRegistry().getMetrics();
        assertThat(metrics).containsKey("aprefixhere.counter1");
        assertThat(metrics).hasSize(extant+1);

        r1.counter("counter2");
        metrics = ActivityMetrics.getMetricRegistry().getMetrics();
        assertThat(metrics).hasSize(extant+2);
        assertThat(metrics).containsKey("aprefixhere.counter2");

        r1.remove("counter1");
        metrics = ActivityMetrics.getMetricRegistry().getMetrics();
        assertThat(metrics).hasSize(extant+1);
        assertThat(metrics).containsKey("aprefixhere.counter2");

        r1.remove("counter2");
        metrics = ActivityMetrics.getMetricRegistry().getMetrics();
        assertThat(metrics).hasSize(extant);

    }

}

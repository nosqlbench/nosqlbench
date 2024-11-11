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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class NBComponentServicesTest {

    @Test
    public void testComponentServices() {
        TestComponent root = new TestComponent("root", "root");
        TestComponent a1 = new TestComponent(root, "a1", "a1");
        TestComponent b1 = new TestComponent(a1, "b1", "b1");

        NBMetricTimer timer1 = a1.create().timer(
            "mfn1",
            3,
            MetricCategory.Verification,
            "testing metric"
        );
        String handle = timer1.getHandle();
        timer1.update(23L, TimeUnit.MILLISECONDS);

        NBMetric foundByHandle = root.find().metric(handle);
        assertThat(foundByHandle).isEqualTo(timer1);

        NBMetric foundByPattern = root.find().metric("name:mfn1");
        assertThat(foundByPattern).isEqualTo(timer1);

        NBFunctionGauge gauge = b1.create().gauge(
            "test_gauge",
            () -> 5.2d,
            MetricCategory.Verification,
            "testing metric"
        );
        String gaugeHandle = gauge.getHandle();

        List<NBMetric> metricsInTree = root.find().metrics();
        assertThat(metricsInTree).containsAll(List.of(timer1, gauge));
        metricsInTree.forEach(m -> {
            System.out.println("metric: " + m.toString());
        });
    }

}

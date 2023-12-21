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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBBaseMetric;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NBMetricsQueryTest {
    private final static TestComponent root = new TestComponent("root","root","type","rootelement");
    private final static TestComponent root_c2 = new TestComponent(root,"c2","c2");
    private final static TestComponent root_c3 = new TestComponent(root,"c3","c3");
    private final static NBMetric m1 = new NBBaseMetric(
        NBLabels.forKV("m1", "m1"),
        "test metric",
        MetricCategory.Verification
    );
    private final static String m1Handle = root.addComponentMetric(
        m1,
        MetricCategory.Verification,
        "testing metric"
    );
    private final static NBMetric m2 = new NBBaseMetric(
        NBLabels.forKV("m2", "m2"),
        "test metric",
        MetricCategory.Verification
        );
    private final static String m2Handle = root_c2.addComponentMetric(
        m2,
        MetricCategory.Verification,
        "testing metric"
    );
    private final static NBMetric m3 = new NBBaseMetric(
        NBLabels.forKV("m3", "m3"),
        "test metric",
        MetricCategory.Verification
    );
    private final static String m3Handle = root_c3.addComponentMetric(
        m3,
        MetricCategory.Verification,
        "testing metric"
    );

    @Test
    public void testFindInTree() {
        NBMetric expectedM3 = root.find().metric("m3:m3");
        assertThat(expectedM3).isEqualTo(m3);
        assertThatThrownBy(() -> root.find().metric("m3:m4")).isOfAnyClassIn(RuntimeException.class);
    }

    @Test
    public void testFindOneInTree() {
        List<NBMetric> metricsInTree = root.find().metrics();
        assertThat(metricsInTree).containsExactly(m1, m2, m3);
        List<NBMetric> m3Only = root.find().metrics("m3:m3");
        assertThat(m3Only).containsExactly(m3);
    }
}

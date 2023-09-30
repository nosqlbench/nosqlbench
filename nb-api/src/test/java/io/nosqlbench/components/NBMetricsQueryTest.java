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

package io.nosqlbench.components;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.api.engine.metrics.instruments.NBBaseMetric;
import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NBMetricsQueryTest {
    private final static TestComponent root = new TestComponent("root","root","type","rootelement");
    private final static TestComponent root_c2 = new TestComponent(root,"c2","c2");
    private final static TestComponent root_c3 = new TestComponent(root,"c3","c3");
    private final static NBMetric m1 = new NBBaseMetric("m1","m1");
    private final String m1Handle = root.addMetric(m1);
    private final static NBMetric m2 = new NBBaseMetric("m2","m2");
    private final String m2Handle = root_c2.addMetric(m2);
    private final static NBMetric m3 = new NBBaseMetric("m3","m3");
    private final String m3Handle = root_c3.addMetric(m3);

    @Test
    public void testFindInTree() {
        NBMetric expectedM3 = root.findOneMetricInTree("m3:m3");
        assertThat(expectedM3).isEqualTo(m3);
        assertThatThrownBy(() -> root.findOneMetricInTree("m3:m4")).isOfAnyClassIn(RuntimeException.class);
    }

    @Test
    public void testFindOneInTree() {
        List<NBMetric> metricsInTree = root.findMetricsInTree("");
        assertThat(metricsInTree).containsExactly(m1, m2, m3);
        List<NBMetric> m3Only = root.findMetricsInTree("m3:m3");
        assertThat(m3Only).containsExactly(m3);
    }
}

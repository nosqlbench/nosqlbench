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

import io.nosqlbench.nb.api.components.core.NBBaseComponentMetrics;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBBaseMetric;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NBBaseComponentMetricsTest {

    @Test
    void testBasicAddAndLookup() {
        NBBaseComponentMetrics cm = new NBBaseComponentMetrics();
        NBMetric m1 = new NBBaseMetric("k","20");
        String m1Handle = cm.addComponentMetric(
            m1,
            MetricCategory.Verification,
            "testing metric"
        );
        NBMetric m2 = new NBBaseMetric("k","27","l","62");
        String m2Handle = cm.addComponentMetric(
            m2,
            MetricCategory.Verification,
            "testing metric"
        );

        assertThat(cm.getComponentMetric(m1Handle)).isEqualTo(m1);
        assertThat(cm.getComponentMetric(m2Handle)).isEqualTo(m2);
    }
    @Test
    void find() {
        NBBaseComponentMetrics cm = new NBBaseComponentMetrics();
        NBMetric m1 = new NBBaseMetric("k","20");
        String m1Handle = cm.addComponentMetric(
            m1,
            MetricCategory.Verification,
            "testing metric"
        );
        NBMetric m2 = new NBBaseMetric("k","27","l","62");
        String m2Handle = cm.addComponentMetric(
            m2,
            MetricCategory.Verification,
            "testing metric"
        );

        assertThat(cm.findComponentMetrics("k=27")).isEqualTo(List.of(m2));
        assertThat(cm.findComponentMetrics("k=20")).isNotEqualTo(List.of(m2));
    }
}

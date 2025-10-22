package io.nosqlbench.nb.api.engine.metrics.reporters;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricInstanceFilterTest {

    private NBMetricCounter counterWith(String name, String activity) {
        return new NBMetricCounter(
            NBLabels.forKV("name", name, "activity", activity, "scenario", "scenario"),
            "counter",
            MetricCategory.Core
        );
    }

    @Test
    void exactLabelMatchIsQuotedAutomatically() {
        MetricInstanceFilter filter = new MetricInstanceFilter()
            .addPattern("name=counter_metric");

        NBMetricCounter matching = counterWith("counter_metric", "activity");
        NBMetricCounter other = counterWith("other_metric", "activity");

        assertThat(filter.matches(matching.getLabels().linearizeAsMetrics(), matching)).isTrue();
        assertThat(filter.matches(other.getLabels().linearizeAsMetrics(), other)).isFalse();
    }

    @Test
    void regexLabelPatternIsSupported() {
        MetricInstanceFilter filter = new MetricInstanceFilter()
            .addPattern("name=counter_.*;activity=act.*");

        NBMetricCounter matching = counterWith("counter_metric", "activity");
        NBMetricCounter other = counterWith("counter_metric", "different");

        assertThat(filter.matches(matching.getLabels().linearizeAsMetrics(), matching)).isTrue();
        assertThat(filter.matches(other.getLabels().linearizeAsMetrics(), other)).isFalse();
    }

    @Test
    void handlePatternStillWorks() {
        MetricInstanceFilter filter = new MetricInstanceFilter()
            .addPattern(".*counter_metric.*");

        NBMetricCounter matching = counterWith("counter_metric", "activity");
        NBMetricCounter other = counterWith("other_metric", "activity");

        assertThat(filter.matches(matching.getLabels().linearizeAsMetrics(), matching)).isTrue();
        assertThat(filter.matches(other.getLabels().linearizeAsMetrics(), other)).isFalse();
    }
}

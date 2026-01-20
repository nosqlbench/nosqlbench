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

package io.nosqlbench.engine.core.script;

import com.codahale.metrics.Histogram;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class MetricsIntegrationTest {

    @Disabled("until this is reimplemented")
    @Test
    public void testHistogramLogger() {
        NBComponent parent = new TestComponent("metricstest","metricstest","alias","foo","driver","diag","op","noop");
        final Histogram testhistogram = parent.create().histogram(
            "testhistogram",
            3,
            MetricCategory.Verification,
            "test metric"
        );
        // TODO: metrics
//        ActivityMetrics.addHistoLogger("testsession", ".*","testhisto.log","1s");
        testhistogram.update(400);
        testhistogram.getSnapshot();
        final File logfile = new File("testhisto.log");
        assertThat(logfile).exists();
        long now = System.currentTimeMillis();
        long millisAge = now - logfile.lastModified();
        assertThat(millisAge).isLessThan(10000L);

    }
}

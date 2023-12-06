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

package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.Timer;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class NBMetricsSummaryTest {

    @Test
    public void testFormat() {
        final StringBuilder sb = new StringBuilder();
        final Timer timer = new Timer(new DeltaHdrHistogramReservoir(NBLabels.forKV("name","test"), 4));

        for (int i = 0; 100000 > i; i++) timer.update(i % 1000 + 1, TimeUnit.MILLISECONDS);

        NBMetricsSummary.summarize(sb, "test", timer);

        System.out.println(sb);
    }

}

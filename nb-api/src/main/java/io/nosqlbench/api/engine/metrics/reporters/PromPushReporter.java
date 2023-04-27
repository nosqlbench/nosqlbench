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

import com.codahale.metrics.*;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class PromPushReporter extends ScheduledReporter {

    protected PromPushReporter(final MetricRegistry registry, final String name, final MetricFilter filter, final TimeUnit rateUnit, final TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
    }

    @Override
    public void report(
        final SortedMap<String, Gauge> gauges,
        final SortedMap<String, Counter> counters,
        final SortedMap<String, Histogram> histograms,
        final SortedMap<String, Meter> meters,
        final SortedMap<String, Timer> timers
    ) {
        String buffer = "";
        gauges.forEach((name,gauge)-> {


        });

        final String exposition = buffer;
    }



}

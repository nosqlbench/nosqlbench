/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.metrics;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramReservoir;

public class EBMetricsRegistry extends MetricRegistry {

    @Override
    public Timer timer(String name) {
        Timer timer = new Timer(new HdrHistogramReservoir());
        return getOrAddMetric(name, timer);
    }

    @Override
    public Histogram histogram(String name) {
        Histogram histogram = new Histogram(new HdrHistogramReservoir());
        return getOrAddMetric(name, histogram);
    }

    @SuppressWarnings("unchecked")
    private <T extends Metric> T getOrAddMetric(String name, T addingMetric) {
        final Metric metric = super.getMetrics().get(name);
        if (metric!=null) {
            return (T) metric;
        } else {
            try {
                return register(name, addingMetric);
            } catch (IllegalArgumentException e) {
                final Metric added = super.getMetrics().get(name);
                if (added!=null) {
                    return (T) added;
                }
                throw new RuntimeException("Metrics 4 will be out when?");
            }
        }
    }

}

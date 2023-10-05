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

import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;

public class NBFinders {
    private final NBBaseComponent base;

    public NBFinders(NBBaseComponent base) {
        this.base = base;
    }

    public NBMetric metric(String pattern) {
        NBMetric metric = base.lookupMetricInTree(pattern);
        if (metric!=null) { return metric; };
        metric = base.findOneMetricInTree(pattern);
        return metric;
    }
    private <T extends NBMetric> T findOneMetricWithType(String pattern, Class<T> clazz) {
        NBMetric found = metric(pattern);
        if (found==null) {
            System.out.println(NBComponentFormats.formatAsTree(base));
            throw new RuntimeException("unable to find metric with pattern '" + pattern + "'");
        }
        if (clazz.isAssignableFrom(found.getClass())) {
            return clazz.cast(found);
        } else {
            throw new RuntimeException(
                "found metric with pattern '" + pattern + "'" +
                    ", but it was type "
                    + found.getClass().getSimpleName() + " (not a "
                    + clazz.getSimpleName() +")"
            );
        }
    }


    public NBMetricGauge metricGauge(String pattern) {
        return findOneMetricWithType(pattern, NBMetricGauge.class);
    }

    public NBMetricCounter metricCounter(String pattern) {
        return findOneMetricWithType(pattern, NBMetricCounter.class);
    }


}

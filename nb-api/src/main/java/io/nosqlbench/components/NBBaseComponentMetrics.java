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

import io.nosqlbench.adapters.api.util.TagFilter;
import io.nosqlbench.api.engine.metrics.instruments.NBMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NBBaseComponentMetrics implements NBComponentMetrics {
    private final Lock lock = new ReentrantLock(false);
    private final Map<String, NBMetric> metrics = new HashMap<>();
    @Override
    public String addMetric(NBMetric metric) {
        try {
            lock.lock();
            String openMetricsName = metric.getLabels().linearizeAsMetrics();
            if (metrics.containsKey(openMetricsName)) {
                throw new RuntimeException("Can't add the same metric by label set to the same live component:" + openMetricsName);
            }
            metrics.put(openMetricsName,metric);
            return metric.getLabels().linearizeAsMetrics();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public NBMetric lookupMetric(String name) {
        return metrics.get(name);
    }

    @Override
    public List<NBMetric> findMetrics(String pattern) {
        TagFilter filter = new TagFilter(pattern);
        return filter.filterLabeled(metrics.values());
    }
}
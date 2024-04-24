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

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.tagging.TagFilter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NBBaseComponentMetrics implements NBComponentMetrics {
    private final Lock lock = new ReentrantLock(false);
    private final Map<String, NBMetric> metrics = new ConcurrentHashMap<>();
    private final static List<MetricRegistryListener> listeners = new CopyOnWriteArrayList<>();
    @Override
    public String addComponentMetric(NBMetric metric, MetricCategory category, String requiredDescription) {
        try {
            lock.lock();
            String openMetricsName = metric.getLabels().linearizeAsMetrics();
            if (metrics.containsKey(openMetricsName)) {
                throw new RuntimeException("Can't add the same metric by label set to the same live component:" + openMetricsName);
            }
            metrics.put(openMetricsName,metric);
            for (MetricRegistryListener listener : listeners) {
                notifyListenerOfAddedMetric(listener, metric, openMetricsName);
            }
            return metric.getLabels().linearizeAsMetrics();
        } finally {
            lock.unlock();
        }
    }

    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);

        for (Map.Entry<String, NBMetric> entry : metrics.entrySet()) {
            notifyListenerOfAddedMetric(listener, entry.getValue(), entry.getKey());
        }
    }

    public void removeListener(MetricRegistryListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenerOfAddedMetric(MetricRegistryListener listener, NBMetric metric, String name) {
        switch (metric) {
            case Gauge gauge -> listener.onGaugeAdded(name, gauge);
            case Counter counter -> listener.onCounterAdded(name, counter);
            case Histogram histogram -> listener.onHistogramAdded(name, histogram);
            case Meter meter -> listener.onMeterAdded(name, meter);
            case Timer timer -> listener.onTimerAdded(name, timer);
            case null, default -> throw new IllegalArgumentException("Unknown metric type: " + metric.getClass());
        }
    }

    private void onMetricRemoved(String name, NBMetric metric) {
        for (MetricRegistryListener listener : listeners) {
            notifyListenerOfRemovedMetric(name, metric, listener);
        }
    }

    private void notifyListenerOfRemovedMetric(String name, NBMetric metric, MetricRegistryListener listener) {
        switch (metric) {
            case Gauge gauge -> listener.onGaugeRemoved(name);
            case Counter counter -> listener.onCounterRemoved(name);
            case Histogram histogram -> listener.onHistogramRemoved(name);
            case Meter meter -> listener.onMeterRemoved(name);
            case Timer timer -> listener.onTimerRemoved(name);
            case null, default -> throw new IllegalArgumentException("Unknown metric type: " + metric.getClass());
        }
    }
    @Override
    public NBMetric getComponentMetric(String name) {
        return metrics.get(name);
    }

    @Override
    public List<NBMetric> findComponentMetrics(String pattern) {
        if (this.metrics.containsKey(pattern)) {
            return List.of(metrics.get(pattern));
        }
        TagFilter filter = new TagFilter(pattern);
        return filter.filterLabeled(metrics.values());
    }

    @Override
    public <T> Collection<? extends T> findComponentMetrics(String pattern, Class<T> type) {
        if (this.metrics.containsKey(pattern)) {
            NBMetric metric = metrics.get(pattern);
            if (type.isAssignableFrom(metric.getClass())) {
                return List.of(type.cast(metric));
            }
        }
        TagFilter filter = new TagFilter(pattern);
        List<NBMetric> found = filter.filterLabeled(metrics.values());
        List<T> foundAndMatching = new ArrayList<>();
        for (NBMetric metric : found) {
            if (type.isAssignableFrom(metric.getClass())) {
                foundAndMatching.add(type.cast(metric));
            }
        }
        return foundAndMatching;
    }


    @Override
    public Collection<? extends NBMetric> getComponentMetrics() {
        return metrics.values();
    }
}

/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A silly class that does nothing but allow cleaner code elsewhere,
 * because MetricRegistryListener, that's why.
 */
public abstract class CapabilityHook<T> implements MetricRegistryListener {

    private final Map<String,T> capables = new HashMap<>();

    public abstract void onCapableAdded(String name, T capable);
    public abstract void onCapableRemoved(String name, T capable);

    protected abstract Class<T> getCapabilityClass();

    @Override
    public void onHistogramAdded(String name, Histogram metric) {
        Class<T> capClass = getCapabilityClass();
        if (capClass.isAssignableFrom(metric.getClass())) {
            T capable = capClass.cast(metric);
            capables.put(name,capable);
            onCapableAdded(name, capable);
        }
    }

    @Override
    public void onHistogramRemoved(String name) {
        T removed = capables.remove(name);
        if (removed!=null) {
            onCapableRemoved(name, removed);
        }
    }

    @Override
    public void onTimerAdded(String name, Timer metric) {
        Class<T> capClass = getCapabilityClass();
        if (capClass.isAssignableFrom(metric.getClass())) {
            T capable = capClass.cast(metric);
            capables.put(name,capable);
            onCapableAdded(name, capable);
        }
    }

    @Override
    public void onTimerRemoved(String name) {
        T removed = capables.remove(name);
        if (removed!=null) {
            onCapableRemoved(name, removed);
        }
    }

    @Override
    public void onGaugeAdded(String name, Gauge<?> metric) {
        Class<T> capClass = getCapabilityClass();
        if (capClass.isAssignableFrom(metric.getClass())) {
            T capable = capClass.cast(metric);
            capables.put(name,capable);
            onCapableAdded(name, capable);
        }
    }

    @Override
    public void onGaugeRemoved(String name) {
        T removed = capables.remove(name);
        if (removed!=null) {
            onCapableRemoved(name, removed);
        }
    }

    @Override
    public void onCounterAdded(String name, Counter metric) {
        Class<T> capClass = getCapabilityClass();
        if (capClass.isAssignableFrom(metric.getClass())) {
            T capable = capClass.cast(metric);
            capables.put(name,capable);
            onCapableAdded(name, capable);
        }
    }

    @Override
    public void onCounterRemoved(String name) {
        T removed = capables.remove(name);
        if (removed!=null) {
            onCapableRemoved(name, removed);
        }
    }

    @Override
    public void onMeterAdded(String name, Meter metric) {
        Class<T> capClass = getCapabilityClass();
        if (capClass.isAssignableFrom(metric.getClass())) {
            T capable = capClass.cast(metric);
            capables.put(name,capable);
            onCapableAdded(name, capable);
        }
    }

    @Override
    public void onMeterRemoved(String name) {
        T removed = capables.remove(name);
        if (removed!=null) {
            onCapableRemoved(name, removed);
        }
    }
}

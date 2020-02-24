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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.*;

public class MetricsRegistryMount implements MetricRegistryListener {

    private final MetricRegistry owningRegistry;

    private final MetricRegistry mountedRegistry;
    private final String mountedPrefix;


    public MetricsRegistryMount(MetricRegistry owningRegistry, MetricRegistry mountedRegistry, String mountedPrefix) {
        this.owningRegistry = owningRegistry;
        this.mountedRegistry = mountedRegistry;
        this.mountedPrefix = mountedPrefix;
        mountedRegistry.addListener(this);
    }
    
    @Override
    public void onGaugeAdded(String name, Gauge<?> gauge) {
        owningRegistry.register(mountedPrefix+name, gauge);
    }

    @Override
    public void onGaugeRemoved(String name) {
        owningRegistry.remove(mountedPrefix+name);
    }

    @Override
    public void onCounterAdded(String name, Counter counter) {
        owningRegistry.register(mountedPrefix+name,counter);
    }

    @Override
    public void onCounterRemoved(String name) {
        owningRegistry.remove(mountedPrefix+name);
    }

    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
        owningRegistry.register(mountedPrefix+name,histogram);
    }

    @Override
    public void onHistogramRemoved(String name) {
        owningRegistry.remove(mountedPrefix+name);
    }

    @Override
    public void onMeterAdded(String name, Meter meter) {
        owningRegistry.register(mountedPrefix+name, meter);
    }

    @Override
    public void onMeterRemoved(String name) {
        owningRegistry.remove(mountedPrefix+name);
    }

    @Override
    public void onTimerAdded(String name, Timer timer) {
        owningRegistry.register(mountedPrefix+name,timer);
    }

    @Override
    public void onTimerRemoved(String name) {
        owningRegistry.remove(mountedPrefix+name);
    }
}

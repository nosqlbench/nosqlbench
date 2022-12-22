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

package io.nosqlbench.engine.core.lifecycle.scenario.script.bindings;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.core.metrics.MetricMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.*;

/**
 * A view of metrics objects as an object tree.
 */
public class PolyglotMetricRegistryBindings implements ProxyObject, MetricRegistryListener {

    private final static Logger logger = LogManager.getLogger("METRICS");

    private final MetricRegistry registry;
    MetricMap metrics = new MetricMap("ROOT",null);

    public PolyglotMetricRegistryBindings(MetricRegistry registry) {
        this.registry = registry;
        registry.addListener(this);
    }

    @Override
    public Object getMember(String key) {
        logger.trace(() -> "get member: " + key);
        Object o = metrics.get(key);
        return o;
    }

    @Override
    public Object getMemberKeys() {
        logger.trace("get member keys");
        ArrayList<String> keys = new ArrayList<>(metrics.getKeys());
        return keys;
    }

    @Override
    public boolean hasMember(String key) {
        logger.trace("has member? " + key);
        boolean b = metrics.containsKey(key);
        return b;
    }

    @Override
    public void putMember(String key, Value value) {
        throw new UnsupportedOperationException("This view is not meant to be modified by users. It is modified " +
            "automatically by metrics registry updates.");
    }

    @Override
    public boolean removeMember(String key) {
        throw new UnsupportedOperationException("This view is not meant to be modified by users. It is modified " +
            "automatically by metrics registry updates.");
    }

    @Override
    public void onGaugeAdded(String name, Gauge<?> gauge) {
        metrics.add(name, gauge);
        logger.debug("gauge added: " + name);
    }

    @Override
    public void onGaugeRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.debug("gauge removed: " + name);
    }

    @Override
    public void onCounterAdded(String name, Counter counter) {
        metrics.add(name, counter);
        logger.debug("counter added: " + name + ", " + counter);
    }

    @Override
    public void onCounterRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.debug("counter removed: " + name);
    }

    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
        metrics.add(name, histogram);
        logger.debug("histogram added: " + name + ", " + histogram);
    }

    @Override
    public void onHistogramRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.debug("histogram removed: " + name);
    }

    @Override
    public void onMeterAdded(String name, Meter meter) {
        metrics.add(name, meter);
        logger.debug("meter added: " + name + ", " + meter);
    }

    @Override
    public void onMeterRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.debug("meter removed: " + name);

    }

    @Override
    public void onTimerAdded(String name, Timer timer) {
        metrics.add(name, timer);
        logger.debug("timer added: " + name);
    }

    @Override
    public void onTimerRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.debug("timer removed: " + name);
    }

    public Map<String, Metric> getMetrics() {
        return getMetrics(new LinkedHashMap<String, Metric>(), "metrics", metrics);
    }

    private Map<String, Metric> getMetrics(Map<String, Metric> totalMap, String prefix, MetricMap map) {
        for (String key : map.getKeys()) {
            Object o = map.get(key);
            String name = prefix + "." + key;
            if (o instanceof Metric) {
                totalMap.put(name, (Metric) o);
            } else if (o instanceof MetricMap) {
                getMetrics(totalMap, name, (MetricMap) o);
            } else {
                throw new RuntimeException("entry value must be either a Metric or a MetricMap");
            }
        }
        return totalMap;
    }

}

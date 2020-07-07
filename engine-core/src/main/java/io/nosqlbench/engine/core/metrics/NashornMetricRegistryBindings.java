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

package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.core.script.ReadOnlyBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class NashornMetricRegistryBindings extends ReadOnlyBindings implements MetricRegistryListener {

    private final static Logger logger = LoggerFactory.getLogger(NashornMetricRegistryBindings.class);

    private final MetricRegistry registry;
    private MetricMap metricMap = new MetricMap("ROOT");
    private boolean failfast = true;

    public NashornMetricRegistryBindings(MetricRegistry registry) {
        this.registry = registry;
        registry.addListener(this);
    }

    @Override
    public int size() {
        return metricMap.map.size();
    }

    @Override
    public boolean isEmpty() {
        return metricMap.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return metricMap.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return metricMap.map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object got = metricMap.map.get(key);
        if (got==null) {
            throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you were looking for one of its children: "
                    + this.keySet().stream().collect(Collectors.joining(",","[","]")));
        }
        return got;
    }

    @Override
    public Set<String> keySet() {
        return metricMap.map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return metricMap.map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return metricMap.map.entrySet();
    }

    @Override
    public void onGaugeAdded(String name, Gauge<?> metric) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.put(nodeNameOf(name), metric);
    }

    private String nodeNameOf(String name) {
        String[] split = name.split("\\.");
        return split[split.length - 1];
    }

    private synchronized void cleanEmptyMaps(MetricMap m) {
        while (m.isEmpty() && m.parent != null) {
            logger.debug("removing empty map:" + m.name);
            MetricMap parent = m.parent;
            m.parent = null;
            parent.map.remove(m.name);
            m = parent;
        }
    }

    private synchronized MetricMap findParentNodeOf(String fullName) {
        String[] names = fullName.split("\\.");
        MetricMap m = metricMap;
        for (int i = 0; i < names.length - 1; i++) {
            String edge = names[i];
            if (m.map.containsKey(edge)) {
                Object o = m.map.get(edge);
                if (o instanceof MetricMap) {
                    m = (MetricMap) m.map.get(edge);
                    logger.trace("traversing edge:" + edge + " while pathing to " + fullName);

                } else {
                    String error = "edge exists at level:" + i + ", while pathing to " + fullName;
                    logger.error(error);
                    if (failfast) {
                        throw new RuntimeException(error);
                    }
                }
            } else {
                MetricMap newMap = new MetricMap(edge, m);
                m.map.put(edge, newMap);
                m = newMap;
                logger.debug("adding edge:" + edge + " while pathing to " + fullName);
            }
        }
        return m;
    }

    @Override
    public void onGaugeRemoved(String name) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.remove(nodeNameOf(name));
        cleanEmptyMaps(parent);
    }

    @Override
    public void onCounterAdded(String name, Counter metric) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.put(nodeNameOf(name), metric);

    }

    @Override
    public void onCounterRemoved(String name) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.remove(nodeNameOf(name));
        cleanEmptyMaps(parent);
    }

    @Override
    public void onHistogramAdded(String name, Histogram metric) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.put(nodeNameOf(name), metric);

    }

    @Override
    public void onHistogramRemoved(String name) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.remove(nodeNameOf(name));
        cleanEmptyMaps(parent);

    }

    @Override
    public void onMeterAdded(String name, Meter metric) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.put(nodeNameOf(name), metric);

    }

    @Override
    public void onMeterRemoved(String name) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.remove(nodeNameOf(name));
        cleanEmptyMaps(parent);

    }

    @Override
    public void onTimerAdded(String name, Timer metric) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.put(nodeNameOf(name), metric);

    }

    @Override
    public void onTimerRemoved(String name) {
        MetricMap parent = findParentNodeOf(name);
        parent.map.remove(nodeNameOf(name));
        cleanEmptyMaps(parent);

    }

    public Map<String, Metric> getMetrics() {
        return getMetrics(new LinkedHashMap<String, Metric>(), "metrics", metricMap);
    }

    private Map<String, Metric> getMetrics(Map<String, Metric> totalMap, String prefix, MetricMap map) {
        for (Entry<String, Object> mEntry : map.entrySet()) {
            Object o = mEntry.getValue();
            String name = prefix + "." + mEntry.getKey();
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

    private class MetricMap extends ReadOnlyBindings {
        Map<String, Object> map = new HashMap<String, Object>();
        MetricMap parent = null;
        public String name;

        MetricMap(String name) {
            this.name = name;
        }

        public MetricMap(String name, MetricMap parent) {
            this.parent = parent;
            this.name = name;
        }

        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            boolean containsKey=map.containsKey(key);
            if (containsKey==false) {
                throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you " +
                        "were looking for one of " + this.map.keySet().stream().collect(Collectors.joining(",","[","]")));
            }
            return true;
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            Object got=map.get(key);
            if (got==null) {
                throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you " +
                        "were looking for one of " + this.map.keySet().stream().collect(Collectors.joining(",","[","]")));
            }
            return got;
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Collection<Object> values() {
            return map.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }

    }
}

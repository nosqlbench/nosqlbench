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

import com.codahale.metrics.Timer;
import com.codahale.metrics.*;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

/**
 * A view of metrics objects as an object tree.
 */
public class PolyglotMetricRegistryBindings implements ProxyObject, MetricRegistryListener {

    private final static Logger logger = LoggerFactory.getLogger(PolyglotMetricRegistryBindings.class);

    private final MetricRegistry registry;
    MetricMap metrics = new MetricMap("ROOT",null);

    public PolyglotMetricRegistryBindings(MetricRegistry registry) {
        this.registry = registry;
        registry.addListener(this);
    }

    @Override
    public Object getMember(String key) {
        logger.info("get member: " + key);
        Object o = metrics.get(key);
        return o;
    }

    @Override
    public Object getMemberKeys() {
        logger.info("get member keys");
        ArrayList<String> keys = new ArrayList<>(metrics.getKeys());
        return keys;
    }

    @Override
    public boolean hasMember(String key) {
        logger.info("has member? " + key);
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
        metrics.add(name,gauge);
        logger.info("gauge added: " + name +", " + gauge);
    }

    @Override
    public void onGaugeRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.info("gauge removed: " + name);
    }

    @Override
    public void onCounterAdded(String name, Counter counter) {
        metrics.add(name,counter);
        logger.info("counter added: " + name + ", " + counter);
    }

    @Override
    public void onCounterRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.info("counter removed: " + name);
    }

    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
        metrics.add(name, histogram);
        logger.info("histogram added: " + name + ", " + histogram);
    }

    @Override
    public void onHistogramRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.info("histogram removed: " + name);
    }

    @Override
    public void onMeterAdded(String name, Meter meter) {
        metrics.add(name,meter);
        logger.info("meter added: " + name + ", " + meter);
    }

    @Override
    public void onMeterRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.info("meter removed: " + name);

    }

    @Override
    public void onTimerAdded(String name, Timer timer) {
        metrics.add(name,timer);
        logger.info("timer added: " + name);
    }

    @Override
    public void onTimerRemoved(String name) {
        metrics.findOwner(name).remove(name);
        logger.info("timer removed: " + name);
    }

//    @Override
//    public int size() {
//        return metricMap.map.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return metricMap.map.isEmpty();
//    }
//
//    @Override
//    public boolean containsKey(Object key) {
//        return metricMap.map.containsKey(key);
//    }
//
//    @Override
//    public boolean containsValue(Object value) {
//        return metricMap.map.containsValue(value);
//    }
//
//    @Override
//    public Object get(Object key) {
//        Object got = metricMap.map.get(key);
//        if (got==null) {
//            throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you were looking for one of its children: "
//                    + this.keySet().stream().collect(Collectors.joining(",","[","]")));
//        }
//        return got;
//    }
//
//    @Override
//    public Set<String> keySet() {
//        return metricMap.map.keySet();
//    }
//
//    @Override
//    public Collection<Object> values() {
//        return metricMap.map.values();
//    }
//
//    @Override
//    public Set<Entry<String, Object>> entrySet() {
//        return metricMap.map.entrySet();
//    }
//
//    @Override
//    public void onGaugeAdded(String name, Gauge<?> metric) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.put(nodeNameOf(name), metric);
//    }
//
//    private String nodeNameOf(String name) {
//        String[] split = name.split("\\.");
//        return split[split.length - 1];
//    }
//
//    private synchronized void cleanEmptyMaps(MetricMap m) {
//        while (m.isEmpty() && m.parent != null) {
//            logger.debug("removing empty map:" + m.name);
//            MetricMap parent = m.parent;
//            m.parent = null;
//            parent.map.remove(m.name);
//            m = parent;
//        }
//    }
//
//    private synchronized MetricMap findParentNodeOf(String fullName) {
//        String[] names = fullName.split("\\.");
//        MetricMap m = metricMap;
//        for (int i = 0; i < names.length - 1; i++) {
//            String edge = names[i];
//            if (m.map.containsKey(edge)) {
//                Object o = m.map.get(edge);
//                if (o instanceof MetricMap) {
//                    m = (MetricMap) m.map.get(edge);
//                    logger.trace("traversing edge:" + edge + " while pathing to " + fullName);
//
//                } else {
//                    String error = "edge exists at level:" + i + ", while pathing to " + fullName;
//                    logger.error(error);
//                    if (failfast) {
//                        throw new RuntimeException(error);
//                    }
//                }
//            } else {
//                MetricMap newMap = new MetricMap(edge, m);
//                m.map.put(edge, newMap);
//                m = newMap;
//                logger.debug("adding edge:" + edge + " while pathing to " + fullName);
//            }
//        }
//        return m;
//    }
//
//    @Override
//    public void onGaugeRemoved(String name) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.remove(nodeNameOf(name));
//        cleanEmptyMaps(parent);
//    }
//
//    @Override
//    public void onCounterAdded(String name, Counter metric) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.put(nodeNameOf(name), metric);
//
//    }
//
//    @Override
//    public void onCounterRemoved(String name) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.remove(nodeNameOf(name));
//        cleanEmptyMaps(parent);
//    }
//
//    @Override
//    public void onHistogramAdded(String name, Histogram metric) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.put(nodeNameOf(name), metric);
//
//    }
//
//    @Override
//    public void onHistogramRemoved(String name) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.remove(nodeNameOf(name));
//        cleanEmptyMaps(parent);
//
//    }
//
//    @Override
//    public void onMeterAdded(String name, Meter metric) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.put(nodeNameOf(name), metric);
//
//    }
//
//    @Override
//    public void onMeterRemoved(String name) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.remove(nodeNameOf(name));
//        cleanEmptyMaps(parent);
//
//    }
//
//    @Override
//    public void onTimerAdded(String name, Timer metric) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.put(nodeNameOf(name), metric);
//
//    }
//
//    @Override
//    public void onTimerRemoved(String name) {
//        MetricMap parent = findParentNodeOf(name);
//        parent.map.remove(nodeNameOf(name));
//        cleanEmptyMaps(parent);
//
//    }
//
//    public Map<String, Metric> getMetrics() {
//        return getMetrics(new LinkedHashMap<String, Metric>(), "metrics", metricMap);
//    }
//
//    private Map<String, Metric> getMetrics(Map<String, Metric> totalMap, String prefix, MetricMap map) {
//        for (Entry<String, Object> mEntry : map.entrySet()) {
//            Object o = mEntry.getValue();
//            String name = prefix + "." + mEntry.getKey();
//            if (o instanceof Metric) {
//                totalMap.put(name, (Metric) o);
//            } else if (o instanceof MetricMap) {
//                getMetrics(totalMap, name, (MetricMap) o);
//            } else {
//                throw new RuntimeException("entry value must be either a Metric or a MetricMap");
//            }
//        }
//        return totalMap;
//    }
//
//    private class MetricMap extends ReadOnlyBindings {
//        Map<String, Object> map = new HashMap<String, Object>();
//        MetricMap parent = null;
//        public String name;
//
//        MetricMap(String name) {
//            this.name = name;
//        }
//
//        public MetricMap(String name, MetricMap parent) {
//            this.parent = parent;
//            this.name = name;
//        }
//
//        public int size() {
//            return map.size();
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return map.isEmpty();
//        }
//
//        @Override
//        public boolean containsKey(Object key) {
//            boolean containsKey=map.containsKey(key);
//            if (containsKey==false) {
//                throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you " +
//                        "were looking for one of " + this.map.keySet().stream().collect(Collectors.joining(",","[","]")));
//            }
//            return true;
//        }
//
//        @Override
//        public boolean containsValue(Object value) {
//            return map.containsValue(value);
//        }
//
//        @Override
//        public Object get(Object key) {
//            Object got=map.get(key);
//            if (got==null) {
//                throw new RuntimeException("Attempted to get metrics node with name '" + key + "', but it was not found. Perhaps you " +
//                        "were looking for one of " + this.map.keySet().stream().collect(Collectors.joining(",","[","]")));
//            }
//            return got;
//        }
//
//        @Override
//        public Set<String> keySet() {
//            return map.keySet();
//        }
//
//        @Override
//        public Collection<Object> values() {
//            return map.values();
//        }
//
//        @Override
//        public Set<Entry<String, Object>> entrySet() {
//            return map.entrySet();
//        }
//
//    }


}

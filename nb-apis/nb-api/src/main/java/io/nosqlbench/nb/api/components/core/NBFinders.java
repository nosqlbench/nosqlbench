/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.nb.api.engine.metrics.instruments.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NBFinders {
    private final NBBaseComponent base;

    public NBFinders(NBBaseComponent base) {
        this.base = base;
    }

    public NBMetric metric(String pattern) {
        return oneMetricInTree(pattern);
    }
    public List<NBMetric> metrics(String pattern) {
        return this.metricsInTree(pattern);
    }
    public List<NBMetric> metrics() {
        return this.metricsInTree();
    }

    private <T extends NBMetric> T oneMetricWithType(String pattern, Class<T> clazz) {
        NBMetric found = metric(pattern);
        if (found==null) {
            System.out.println(NBComponentFormats.formatAsTree(base));
            throw new RuntimeException("unable to find metric with pattern '" + pattern + "'");
        }
        if (clazz.isInstance(found.getClass())) {
            return clazz.cast(found);
        } else if (clazz.isAssignableFrom(found.getClass())) {
            return clazz.cast(found);
        } else {
            throw new RuntimeException(
                "found metric with pattern '" + pattern + "'" +
                    ", but it was type "
                    + found.getClass().getSimpleName() + " (not a "
                    + clazz.getSimpleName() + ")"
            );
        }
    }

    public static <T extends NBMetric> List<T> allMetricsWithType(Class<T> clazz, NBComponent component) {
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(component);
        List<NBMetric> found = new ArrayList<>();
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            for (NBMetric metric : c.getComponentMetrics()) {
                if (clazz.isAssignableFrom(metric.getClass())) {
                    found.add(metric);
                }
            }
        }
        return (List<T>) found;
    }

    public NBMetricGauge gauge(String pattern) {
        return oneMetricWithType(pattern, NBMetricGauge.class);
    }

    public NBMetricCounter counter(String pattern) {
        return oneMetricWithType(pattern, NBMetricCounter.class);
    }

    public NBMetricTimer timer(String pattern) {
        return oneMetricWithType(pattern, NBMetricTimer.class);
    }

    public NBMetricHistogram histogram(String pattern) {
        return oneMetricWithType(pattern, NBMetricHistogram.class);
    }

    public NBMetricMeter meter(String pattern) {
        return oneMetricWithType(pattern,NBMetricMeter.class);
    }

//    public NBMetric firstMetricInTree(String name) {
//        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(base);
//        while (tree.hasNext()) {
//            NBComponent c = tree.next();
//            NBMetric metric = base.getComponentMetric(name);
//            if (metric != null) return metric;
//        }
//        return null;
//    }

    private List<NBMetric> metricsInTree() {
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(base);
        List<NBMetric> found = new ArrayList<>();
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            found.addAll(c.getComponentMetrics());
        }
        return found;
    }
    private List<NBMetric> metricsInTree(String pattern) {
        if (pattern.isEmpty()) {
            throw new RuntimeException("non-empty predicate is required for this form. Perhaps you wanted metricsInTree()");
        }
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(base);
        List<NBMetric> found = new ArrayList<>();
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            found.addAll(c.findComponentMetrics(pattern));
        }
        return found;
    }

    private <T> List<T> metricsInTree(String pattern, Class<T> type) {
        if (pattern.isEmpty()) {
            throw new RuntimeException("non-empty predicate is required for this form. Perhaps you wanted metricsInTree()");
        }
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(base);
        List<T> found = new ArrayList<>();
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            found.addAll(c.findComponentMetrics(pattern,type));
        }
        return found;
    }


    private NBMetric oneMetricInTree(String pattern) {
        List<NBMetric> found = metricsInTree(pattern);
        if (found.size() != 1) {
            System.out.println(
                "Runtime Components and Metrics at this time:\n" + NBComponentFormats.formatAsTree(base));
            throw new RuntimeException("""
          Found COUNT metrics with pattern 'PATTERN', expected exactly 1.
          LIST
          """.replace("COUNT", found.size() + "").replace("PATTERN", pattern)
                .replace("LIST", found.stream().map(Object::toString).collect(Collectors.joining("\n"))));
        }
        return found.getFirst();
    }

    public <T extends NBMetric> T topMetric(String pattern, Class<T> type) {
        List<T> metrics = this.metricsInTree(pattern, type);
        if (!metrics.isEmpty()) {
            return metrics.get(0);
        } else {
            throw new RuntimeException("top metric not found: pattern='" +pattern +"', type='" + type.getSimpleName() + "'");
        }
    }

}

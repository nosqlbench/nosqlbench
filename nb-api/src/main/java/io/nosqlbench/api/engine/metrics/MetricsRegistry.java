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
 *
 */

package io.nosqlbench.api.engine.metrics;

import com.codahale.metrics.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public interface MetricsRegistry {

    public Map<String, Metric> getMetrics();

    public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException;

    public <T> Gauge<T> registerGauge(String name, Gauge<T> metric) throws IllegalArgumentException;

    public void registerAll(MetricSet metrics) throws IllegalArgumentException;

    public Counter counter(String name);

    public Counter counter(String name, final MetricRegistry.MetricSupplier<Counter> supplier);
    public void addListener(MetricRegistryListener listener);

    public Histogram histogram(String name);

    public Histogram histogram(String name, final MetricRegistry.MetricSupplier<Histogram> supplier);

    public Meter meter(String name);

    public Meter meter(String name, final MetricRegistry.MetricSupplier<Meter> supplier);

    public Timer timer(String name);

    public Timer timer(String name, final MetricRegistry.MetricSupplier<Timer> supplier);

    public <T extends Gauge> T gauge(String name);

    public <T extends Gauge> T gauge(String name, final MetricRegistry.MetricSupplier<T> supplier);

    public boolean remove(String name);

    public void removeMatching(MetricFilter filter);

    public void removeListener(MetricRegistryListener listener);

    public SortedSet<String> getNames();

    public SortedMap<String, Gauge> getGauges();

    public SortedMap<String, Gauge> getGauges(MetricFilter filter);

    public SortedMap<String, Counter> getCounters();

    public SortedMap<String, Counter> getCounters(MetricFilter filter);

    public SortedMap<String, Histogram> getHistograms();

    public SortedMap<String, Histogram> getHistograms(MetricFilter filter);

    public SortedMap<String, Meter> getMeters();

    public SortedMap<String, Meter> getMeters(MetricFilter filter);

    public SortedMap<String, Timer> getTimers();

    public SortedMap<String, Timer> getTimers(MetricFilter filter);


}

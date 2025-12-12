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

package io.nosqlbench.nb.api.engine.metrics;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.UnstartedPeriodicTaskComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinates metric sampling across a component tree so every reporting channel receives consistent
 * {@link MetricsView} snapshots.
 *
 * <p>The scheduler lives at the session root and captures immutable snapshots on a base cadence. Reporters register
 * interest at specific cadences; the scheduler aggregates finer-grained snapshots to satisfy slower intervals.</p>
 *
 * <pre>
 * Metrics Sources (root component) ── capture() ──► MetricsSnapshotScheduler (base interval)
 *                                                   └─► ScheduleNode (base)
 *                                                        ├─► ScheduleNode 100 ms ──► Console, CSV, SQLite…
 *                                                        └─► ScheduleNode 300 ms ──► Log4J, Prometheus…
 * </pre>
 *
 * <p>Cadence rules:</p>
 * <ul>
 *   <li>The first registration sets the base interval.</li>
 *   <li>Every subsequent interval must be an exact multiple of the base.</li>
 *   <li>Cadences are represented as a divisor-tree rooted at the base interval. Each node accumulates and combines
 *   snapshots from its parent interval before emitting.</li>
 * </ul>
 *
 * <p>At runtime you can adjust the schedule simply by registering new reporters:</p>
 * <ul>
 *   <li>Registering a faster cadence (&lt; current base) rebuilds the scheduler and aligns all existing consumers to the new base.</li>
 *   <li>Registering or removing slower cadences (multiples of the base) updates the aggregation tree on the fly.</li>
 *   <li>Consumers may unregister when they no longer need snapshots; idle cadences are automatically pruned.</li>
 * </ul>
 *
 * <p>All concrete reporters extend {@link MetricsSnapshotReporterBase}, which handles registration and lifecycle wiring so the reporter only needs to format/output each snapshot.</p>
 */
public final class MetricsSnapshotScheduler extends UnstartedPeriodicTaskComponent {

    private static final Logger logger = LogManager.getLogger(MetricsSnapshotScheduler.class);

    public interface MetricsSnapshotConsumer {
        void onMetricsSnapshot(MetricsView view);

        /**
         * Called to provide session-level metadata associated with a label set.
         * This metadata persists across the session and can include information like:
         * - NoSQLBench version
         * - Command-line arguments
         * - Hardware/system information
         *
         * @param labels The labels identifying the context for this metadata
         * @param metadata Key-value pairs of metadata to store
         */
        default void onSessionMetadata(NBLabels labels, Map<String, String> metadata) {
            // Default no-op for implementations that don't need metadata storage
        }
    }

    private static final ConcurrentHashMap<NBComponent, MetricsSnapshotScheduler> schedulers =
        new ConcurrentHashMap<>();

    public static MetricsSnapshotScheduler register(NBComponent component,
                                                    long intervalMillis,
                                                    MetricsSnapshotConsumer consumer) {
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(consumer, "consumer");
        if (intervalMillis <= 0L) {
            throw new IllegalArgumentException("Interval must be > 0, was " + intervalMillis);
        }
        NBComponent root = findRoot(component);
        MetricsSnapshotScheduler[] created = new MetricsSnapshotScheduler[1];
        MetricsSnapshotScheduler scheduler = schedulers.compute(root, (key, existing) -> {
            if (existing == null) {
                MetricsSnapshotScheduler createdScheduler = new MetricsSnapshotScheduler(root, intervalMillis);
                created[0] = createdScheduler;
                return createdScheduler;
            }
            if (intervalMillis < existing.baseIntervalMillis) {
                if (existing.baseIntervalMillis % intervalMillis != 0) {
                    throw new IllegalArgumentException(
                        "Requested interval " + intervalMillis + " must divide existing base interval " + existing.baseIntervalMillis);
                }
                List<ScheduleRegistration> registrations = existing.snapshotSchedules();
                existing.teardown();
                MetricsSnapshotScheduler replacement = new MetricsSnapshotScheduler(root, intervalMillis);
                replacement.restoreSchedules(registrations);
                created[0] = replacement;
                return replacement;
            }
            existing.assertCompatible(intervalMillis);
            return existing;
        });
        scheduler.registerInterval(intervalMillis, consumer);
        if (created[0] != null) {
            created[0].start();
        }
        return scheduler;
    }

    private static NBComponent findRoot(NBComponent component) {
        NBComponent cursor = component;
        while (cursor.getParent() != null) {
            cursor = cursor.getParent();
        }
        return cursor;
    }

    private final NavigableMap<Long, ScheduleNode> scheduleNodes = new TreeMap<>();
    private final ConcurrentHashMap<MetricsSnapshotConsumer, Long> consumerIntervals = new ConcurrentHashMap<>();
    private final Object scheduleLock = new Object();
    private final long baseIntervalMillis;

    private MetricsSnapshotScheduler(NBComponent root, long intervalMillis) {
        super(root,
            NBLabels.forKV("_type", "metrics_snapshot_scheduler"),
            intervalMillis,
            "METRICS-SNAPSHOT",
            FirstReport.OnInterval,
            LastReport.None);
        this.baseIntervalMillis = intervalMillis;
        scheduleNodes.put(intervalMillis, new ScheduleNode(intervalMillis));
    }

    public long getIntervalMillis() {
        return baseIntervalMillis;
    }

    static MetricsSnapshotScheduler lookup(NBComponent component) {
        NBComponent root = findRoot(component);
        return schedulers.get(root);
    }

    private List<ScheduleRegistration> snapshotSchedules() {
        List<ScheduleRegistration> registrations = new ArrayList<>();
        synchronized (scheduleLock) {
            for (Map.Entry<MetricsSnapshotConsumer, Long> entry : consumerIntervals.entrySet()) {
                registrations.add(new ScheduleRegistration(entry.getValue(), entry.getKey()));
            }
        }
        registrations.sort((a, b) -> Long.compare(a.intervalMillis(), b.intervalMillis()));
        return registrations;
    }

    private void restoreSchedules(List<ScheduleRegistration> registrations) {
        synchronized (scheduleLock) {
            for (ScheduleRegistration registration : registrations) {
                assertCompatible(registration.intervalMillis());
                consumerIntervals.put(registration.consumer(), registration.intervalMillis());
            }
            rebuildScheduleTreeLocked();
        }
    }

    private void assertCompatible(long intervalMillis) {
        if (intervalMillis < baseIntervalMillis) {
            throw new IllegalArgumentException("Interval " + intervalMillis + " is smaller than base interval " + baseIntervalMillis);
        }
        if (intervalMillis % baseIntervalMillis != 0) {
            throw new IllegalArgumentException("Interval " + intervalMillis + " must be a multiple of base interval " + baseIntervalMillis);
        }
    }

    private void registerInterval(long intervalMillis, MetricsSnapshotConsumer consumer) {
        synchronized (scheduleLock) {
            assertCompatible(intervalMillis);
            consumerIntervals.put(consumer, intervalMillis);
            rebuildScheduleTreeLocked();
        }
    }

    public void unregisterConsumer(MetricsSnapshotConsumer consumer) {
        synchronized (scheduleLock) {
            Long interval = consumerIntervals.remove(consumer);
            if (interval == null) {
                return;
            }
            rebuildScheduleTreeLocked();
        }
    }

    private void rebuildScheduleTreeLocked() {
        scheduleNodes.clear();

        NavigableMap<Long, ScheduleNode> nodes = new TreeMap<>();
        for (Long interval : distinctIntervalsLocked()) {
            nodes.put(interval, new ScheduleNode(interval));
        }
        for (Map.Entry<MetricsSnapshotConsumer, Long> entry : consumerIntervals.entrySet()) {
            ScheduleNode node = nodes.get(entry.getValue());
            if (node != null) {
                node.addConsumer(entry.getKey());
            }
        }

        for (Long interval : nodes.keySet()) {
            if (interval == baseIntervalMillis) {
                continue;
            }
            long parentInterval = baseIntervalMillis;
            for (Long candidate : nodes.headMap(interval, false).descendingKeySet()) {
                if (interval % candidate == 0L) {
                    parentInterval = candidate;
                    break;
                }
            }
            nodes.get(parentInterval).addChild(nodes.get(interval));
        }

        scheduleNodes.putAll(nodes);
    }

    private List<Long> distinctIntervalsLocked() {
        TreeMap<Long, Boolean> unique = new TreeMap<>();
        unique.put(baseIntervalMillis, Boolean.TRUE);
        for (Long interval : consumerIntervals.values()) {
            unique.put(interval, Boolean.TRUE);
        }
        return new ArrayList<>(unique.keySet());
    }


    @Override
    protected void task() {
        if (consumerIntervals.isEmpty()) {
            return;
        }
        List<NBMetric> metrics = new ArrayList<>(getParent().find().metrics());
        if (metrics.isEmpty()) {
            return;
        }
        MetricsView snapshot = MetricsView.capture(metrics, baseIntervalMillis);
        processSnapshot(snapshot);
    }

    void injectSnapshotForTesting(MetricsView view) {
        processSnapshot(view);
    }

    private void processSnapshot(MetricsView view) {
        if (view == null) {
            return;
        }
        synchronized (scheduleLock) {
            ScheduleNode base = scheduleNodes.get(baseIntervalMillis);
            if (base != null) {
                base.ingest(view);
            }
        }
    }

    @Override
    public void teardown() {
        NBComponent parent = getParent();
        if (parent != null) {
            parent.detachChild(this);
        }
        schedulers.values().removeIf(scheduler -> scheduler == this);
        consumerIntervals.clear();
        synchronized (scheduleLock) {
            scheduleNodes.clear();
        }
        super.teardown();
    }

    private record ScheduleRegistration(long intervalMillis, MetricsSnapshotConsumer consumer) {
    }

    private static final class ScheduleNode {
        private final long intervalMillis;
        private final CopyOnWriteArrayList<MetricsSnapshotConsumer> consumers = new CopyOnWriteArrayList<>();
        private final List<ScheduleNode> children = new ArrayList<>();
        private MetricsView pending;
        private long accumulatedMillis = 0L;

        private ScheduleNode(long intervalMillis) {
            this.intervalMillis = intervalMillis;
        }

        private void addConsumer(MetricsSnapshotConsumer consumer) {
            consumers.addIfAbsent(consumer);
        }

        private void addChild(ScheduleNode child) {
            children.add(child);
        }

        private void ingest(MetricsView view) {
            if (view == null) {
                return;
            }
            if (pending == null) {
                pending = view;
            } else {
                pending = MetricsView.combine(Arrays.asList(pending, view));
            }
            accumulatedMillis += view.intervalMillis();
            if (accumulatedMillis >= intervalMillis) {
                MetricsView ready = pending;
                pending = null;
                accumulatedMillis = accumulatedMillis - intervalMillis;
                emit(ready);
                for (ScheduleNode child : children) {
                    child.ingest(ready);
                }
                return;
            }
        }

        private void emit(MetricsView view) {
            for (MetricsSnapshotConsumer consumer : consumers) {
                try {
                    consumer.onMetricsSnapshot(view);
                } catch (Exception e) {
                    logger.warn("Metrics snapshot consumer {} threw exception", consumer.getClass().getSimpleName(), e);
                }
            }
        }
    }
}

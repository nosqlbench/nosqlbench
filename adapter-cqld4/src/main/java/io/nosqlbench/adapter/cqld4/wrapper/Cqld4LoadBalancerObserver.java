/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.cqld4.wrapper;

import com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.session.Session;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Cqld4LoadBalancerObserver implements LoadBalancingPolicy {
    private final static Logger logger = LogManager.getLogger("NODELOG");
    private final Map<String, Bucket> buffer = new ConcurrentHashMap<>();
    private final Map<String, Bucket> totals = new ConcurrentHashMap<>();

    private NodeSummary summarizer = NodeSummary.none;

    private final LoadBalancingPolicy delegate;

    private final long minReportGap = 1000;
    private long lastReportTimeMs = System.currentTimeMillis();
    private final long maxReportCount = 1000;
    private AtomicLong untallied = new AtomicLong();

    public Cqld4LoadBalancerObserver(LoadBalancingPolicy delegate, NodeSummary summarizer) {
        logger.info("Loading CQL diagnostic layer");
        this.delegate = delegate;
        this.summarizer = summarizer;

    }

    @Override
    public void init(@NotNull Map<UUID, Node> nodes, @NotNull LoadBalancingPolicy.DistanceReporter distanceReporter) {
        delegate.init(nodes, distanceReporter);
    }

    @NotNull
    @Override
    public Queue<Node> newQueryPlan(@Nullable Request request, @Nullable Session session) {
        Queue<Node> nodeQueue = delegate.newQueryPlan(request, session);
        tabulate(nodeQueue);
        return nodeQueue;
    }

    private void tabulate(Queue<Node> nodeQueue) {
        untallied.getAndAdd(1);

        StringBuilder sb = new StringBuilder();
        Iterator<Node> nodes = nodeQueue.iterator();

        while (nodes.hasNext()) {
            Node node = nodes.next();
            String bcname = node.getBroadcastAddress().map(InetSocketAddress::toString).orElse("UNSET");
            buffer.computeIfAbsent(bcname, Bucket::new).increment();
            sb.append("\n").append(NodeSummary.mid.summarize(node));
            sb.append(" ;; ");
        }



//        sb.setLength(sb.length()-" ;; ".length());
//        String summary = sb.toString();
//        logger.info(summary);
    }

    public synchronized void checkpoint() {
        emitSummary("window", buffer);
        buffer.forEach((k,v) -> {
            totals.computeIfAbsent(k,n -> new Bucket(k)).add(v);
        });
        buffer.clear();
        emitSummary("totals", totals);
    }

    private void emitSummary(String desc, Map<String, Bucket> buffer) {
        List<Bucket> values = new ArrayList<Bucket>(buffer.values());
        Collections.sort(values);
        StringBuilder sb = new StringBuilder();
        for (Bucket bucket : values) {
            sb.append(bucket.summary()).append(" ");
        }
        logger.info("node selection: (" + desc + ") " + sb.toString());

    }

    @Override
    public void onAdd(@NotNull Node node) {
        delegate.onAdd(node);
    }

    @Override
    public void onUp(@NotNull Node node) {
        delegate.onUp(node);
    }

    @Override
    public void onDown(@NotNull Node node) {
        delegate.onDown(node);
    }

    @Override
    public void onRemove(@NotNull Node node) {
        delegate.onRemove(node);
    }

    @Override
    public void close() {
        delegate.close();
    }

    public static class Bucket implements Comparable<Bucket> {
        public long nanotime = System.nanoTime();
        public String name;
        public long count;

        public Bucket(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Bucket b) {
                return name.equals(b.name);
            }
            return false;
        }

        public synchronized void increment() {
            count++;
        }

        public void add(Bucket v) {
            count+=v.count;
        }

        @Override
        public int compareTo(@NotNull Cqld4LoadBalancerObserver.Bucket o) {
            return this.name.compareTo(o.name);
        }

        public String summary() {
            return name + ":" + count;
        }
    }
}

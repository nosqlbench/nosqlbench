/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.api.engine.metrics;

import com.codahale.metrics.Timer;
import io.nosqlbench.api.config.NBLabeledElement;
import org.HdrHistogram.Histogram;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class NicerTimer extends Timer implements DeltaSnapshotter, HdrDeltaHistogramAttachment, TimerAttachment, NBLabeledElement {
    private final String metricName;
    private final DeltaHdrHistogramReservoir deltaHdrHistogramReservoir;
    private long cacheExpiry;
    private List<Timer> mirrors;
    private final MapLabels labels;

    public NicerTimer(final String metricName, final DeltaHdrHistogramReservoir deltaHdrHistogramReservoir) {
        super(deltaHdrHistogramReservoir);
        labels = NBLabeledElement.forMap(Map.of("name",metricName));
        this.metricName = metricName;
        this.deltaHdrHistogramReservoir = deltaHdrHistogramReservoir;
    }

    @Override
    public ConvenientSnapshot getSnapshot() {
        if (System.currentTimeMillis() >= this.cacheExpiry)
            return new ConvenientSnapshot(this.deltaHdrHistogramReservoir.getSnapshot());
        return new ConvenientSnapshot(this.deltaHdrHistogramReservoir.getLastSnapshot());
    }

    @Override
    public DeltaSnapshotReader getDeltaReader() {
        return new DeltaSnapshotReader(this);
    }

    @Override
    public ConvenientSnapshot getDeltaSnapshot(final long cacheTimeMillis) {
        cacheExpiry = System.currentTimeMillis() + cacheTimeMillis;
        return new ConvenientSnapshot(this.deltaHdrHistogramReservoir.getSnapshot());
    }

    @Override
    public synchronized NicerTimer attachHdrDeltaHistogram() {
        if (null == mirrors) this.mirrors = new CopyOnWriteArrayList<>();
        final DeltaHdrHistogramReservoir sameConfigReservoir = deltaHdrHistogramReservoir.copySettings();
        final NicerTimer mirror = new NicerTimer(metricName, sameConfigReservoir);
        this.mirrors.add(mirror);
        return mirror;
    }
    @Override
    public Timer attachTimer(final Timer timer) {
        if (null == mirrors) this.mirrors = new CopyOnWriteArrayList<>();
        this.mirrors.add(timer);
        return timer;
    }


    @Override
    public Histogram getNextHdrDeltaHistogram() {
        return deltaHdrHistogramReservoir.getNextHdrHistogram();
    }

    @Override
    public void update(final long duration, final TimeUnit unit) {
        super.update(duration, unit);
        if (null != mirrors) for (final Timer mirror : this.mirrors) mirror.update(duration, unit);
    }

    @Override
    public Map<String, String> getLabels() {
        return labels.getLabels();
    }
}

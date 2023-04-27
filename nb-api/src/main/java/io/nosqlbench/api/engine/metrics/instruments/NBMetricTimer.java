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

package io.nosqlbench.api.engine.metrics.instruments;

import com.codahale.metrics.Timer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.*;
import org.HdrHistogram.Histogram;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class NBMetricTimer extends Timer implements DeltaSnapshotter, HdrDeltaHistogramAttachment, TimerAttachment, NBLabeledElement {
    private final DeltaHdrHistogramReservoir deltaHdrHistogramReservoir;
    private long cacheExpiry;
    private List<Timer> mirrors;
    private final Map<String,String> labels;

    public NBMetricTimer(Map<String,String> labels, DeltaHdrHistogramReservoir deltaHdrHistogramReservoir) {
        super(deltaHdrHistogramReservoir);
        this.labels = labels;
        this.deltaHdrHistogramReservoir = deltaHdrHistogramReservoir;
    }

    @Override
    public ConvenientSnapshot getSnapshot() {
        if (System.currentTimeMillis() >= cacheExpiry) {
            return new ConvenientSnapshot(deltaHdrHistogramReservoir.getSnapshot());
        }
        return new ConvenientSnapshot(deltaHdrHistogramReservoir.getLastSnapshot());
    }

    @Override
    public DeltaSnapshotReader getDeltaReader() {
        return new DeltaSnapshotReader(this);
    }

    @Override
    public ConvenientSnapshot getDeltaSnapshot(long cacheTimeMillis) {
        this.cacheExpiry = System.currentTimeMillis() + cacheTimeMillis;
        return new ConvenientSnapshot(deltaHdrHistogramReservoir.getSnapshot());
    }

    @Override
    public synchronized NBMetricTimer attachHdrDeltaHistogram() {
        if (null == this.mirrors) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        DeltaHdrHistogramReservoir sameConfigReservoir = this.deltaHdrHistogramReservoir.copySettings();
        NBMetricTimer mirror = new NBMetricTimer(this.labels, sameConfigReservoir);
        mirrors.add(mirror);
        return mirror;
    }
    @Override
    public Timer attachTimer(Timer timer) {
        if (null == this.mirrors) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        mirrors.add(timer);
        return timer;
    }


    @Override
    public Histogram getNextHdrDeltaHistogram() {
        return this.deltaHdrHistogramReservoir.getNextHdrHistogram();
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        super.update(duration, unit);
        if (null != this.mirrors) {
            for (Timer mirror : mirrors) {
                mirror.update(duration, unit);
            }
        }
    }

    @Override
    public Map<String, String> getLabels() {
        return this.labels;
    }
}

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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Histogram;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class NicerHistogram extends Histogram implements DeltaSnapshotter, HdrDeltaHistogramAttachment, HistogramAttachment {

    private final DeltaHdrHistogramReservoir hdrDeltaReservoir;
    private long cacheExpiryMillis = 0L;
    private long cacheTimeMillis = 0L;
    private final String metricName;
    private List<Histogram> mirrors;

    public NicerHistogram(String metricName, DeltaHdrHistogramReservoir hdrHistogramReservoir) {
        super(hdrHistogramReservoir);
        this.metricName = metricName;
        this.hdrDeltaReservoir = hdrHistogramReservoir;
    }

    @Override
    public DeltaSnapshotReader getDeltaReader() {
        return new DeltaSnapshotReader(this);
    }

    /**
     * Only return a new snapshot form current reservoir data if the cached one has expired.
     *
     * @return a new delta snapshot, or the cached one
     */
    @Override
    public ConvenientSnapshot getSnapshot() {
        if (System.currentTimeMillis() < cacheExpiryMillis) {
            return new ConvenientSnapshot(hdrDeltaReservoir.getLastSnapshot());
        } else {
            return new ConvenientSnapshot(hdrDeltaReservoir.getSnapshot());
        }
    }

    public ConvenientSnapshot getDeltaSnapshot(long cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
        cacheExpiryMillis = System.currentTimeMillis() + this.cacheTimeMillis;
        ConvenientSnapshot convenientSnapshot = new ConvenientSnapshot(hdrDeltaReservoir.getSnapshot());
        return convenientSnapshot;
    }

    @Override
    public synchronized NicerHistogram attachHdrDeltaHistogram() {
        if (mirrors == null) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        DeltaHdrHistogramReservoir mirrorReservoir = this.hdrDeltaReservoir.copySettings();
        NicerHistogram mirror = new NicerHistogram("mirror-" + this.metricName, mirrorReservoir);
        mirrors.add(mirror);
        return mirror;
    }

    @Override
    public Histogram attachHistogram(Histogram histogram) {
        if (mirrors == null) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        mirrors.add(histogram);
        return histogram;
    }

    @Override
    public void update(long value) {
        super.update(value);
        if (mirrors != null) {
            for (Histogram mirror : mirrors) {
                mirror.update(value);
            }
        }
    }

    @Override
    public org.HdrHistogram.Histogram getNextHdrDeltaHistogram() {
        return hdrDeltaReservoir.getNextHdrHistogram();
    }

}

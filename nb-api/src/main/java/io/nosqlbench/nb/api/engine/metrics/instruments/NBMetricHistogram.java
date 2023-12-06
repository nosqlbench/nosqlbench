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

package io.nosqlbench.nb.api.engine.metrics.instruments;

import com.codahale.metrics.Histogram;
import io.nosqlbench.nb.api.engine.metrics.*;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class NBMetricHistogram extends Histogram implements DeltaSnapshotter, HdrDeltaHistogramAttachment, HistogramAttachment, NBMetric {

    private final DeltaHdrHistogramReservoir hdrDeltaReservoir;
    private final NBLabels labels;
    private long cacheExpiryMillis;
    private long cacheTimeMillis;
    private List<Histogram> mirrors;

    public NBMetricHistogram(NBLabels labels, DeltaHdrHistogramReservoir hdrHistogramReservoir) {
        super(hdrHistogramReservoir);
        this.labels = labels;
        this.hdrDeltaReservoir = hdrHistogramReservoir;
    }

    public NBMetricHistogram(String name, DeltaHdrHistogramReservoir hdrHistogramReservoir) {
        super(hdrHistogramReservoir);
        this.labels = NBLabels.forKV("name",name);
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
        }
        return new ConvenientSnapshot(hdrDeltaReservoir.getSnapshot());
    }

    @Override
    public ConvenientSnapshot getDeltaSnapshot(long cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
        cacheExpiryMillis = System.currentTimeMillis() + this.cacheTimeMillis;
        ConvenientSnapshot convenientSnapshot = new ConvenientSnapshot(hdrDeltaReservoir.getSnapshot());
        return convenientSnapshot;
    }

    @Override
    public synchronized NBMetricHistogram attachHdrDeltaHistogram() {
        if (null == this.mirrors) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        DeltaHdrHistogramReservoir mirrorReservoir = this.hdrDeltaReservoir.copySettings();
        NBMetricHistogram mirror = new NBMetricHistogram("mirror-" + this.labels.linearizeValues("name"), mirrorReservoir);
        mirrors.add(mirror);
        return mirror;
    }

    @Override
    public Histogram attachHistogram(Histogram histogram) {
        if (null == this.mirrors) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        mirrors.add(histogram);
        return histogram;
    }

    @Override
    public void update(long value) {
        super.update(value);
        if (null != this.mirrors) {
            for (Histogram mirror : mirrors) {
                mirror.update(value);
            }
        }
    }

    @Override
    public org.HdrHistogram.Histogram getNextHdrDeltaHistogram() {
        return hdrDeltaReservoir.getNextHdrHistogram();
    }

    @Override
    public NBLabels getLabels() {
        return this.labels;
    }

    @Override
    public String typeName() {
        return "histogram";
    }

    @Override
    public String toString() {
        return description();
    }
}

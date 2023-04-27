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

import com.codahale.metrics.Histogram;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class NBMetricHistogram extends Histogram implements DeltaSnapshotter, HdrDeltaHistogramAttachment, HistogramAttachment, NBLabeledElement {

    private final DeltaHdrHistogramReservoir hdrDeltaReservoir;
    private final Map<String, String> labels;
    private long cacheExpiryMillis;
    private long cacheTimeMillis;
    private List<Histogram> mirrors;

    public NBMetricHistogram(final Map<String,String> labels, final DeltaHdrHistogramReservoir hdrHistogramReservoir) {
        super(hdrHistogramReservoir);
        this.labels = labels;
        hdrDeltaReservoir = hdrHistogramReservoir;
    }

    public NBMetricHistogram(final String name, final DeltaHdrHistogramReservoir hdrHistogramReservoir) {
        super(hdrHistogramReservoir);
        labels = Map.of("name",name);
        hdrDeltaReservoir = hdrHistogramReservoir;
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
        if (System.currentTimeMillis() < this.cacheExpiryMillis)
            return new ConvenientSnapshot(this.hdrDeltaReservoir.getLastSnapshot());
        return new ConvenientSnapshot(this.hdrDeltaReservoir.getSnapshot());
    }

    @Override
    public ConvenientSnapshot getDeltaSnapshot(final long cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
        this.cacheExpiryMillis = System.currentTimeMillis() + this.cacheTimeMillis;
        final ConvenientSnapshot convenientSnapshot = new ConvenientSnapshot(this.hdrDeltaReservoir.getSnapshot());
        return convenientSnapshot;
    }

    @Override
    public synchronized NBMetricHistogram attachHdrDeltaHistogram() {
        if (null == mirrors) this.mirrors = new CopyOnWriteArrayList<>();
        final DeltaHdrHistogramReservoir mirrorReservoir = hdrDeltaReservoir.copySettings();
        final NBMetricHistogram mirror = new NBMetricHistogram("mirror-" + labels.get("name"), mirrorReservoir);
        this.mirrors.add(mirror);
        return mirror;
    }

    @Override
    public Histogram attachHistogram(final Histogram histogram) {
        if (null == mirrors) this.mirrors = new CopyOnWriteArrayList<>();
        this.mirrors.add(histogram);
        return histogram;
    }

    @Override
    public void update(final long value) {
        super.update(value);
        if (null != mirrors) for (final Histogram mirror : this.mirrors) mirror.update(value);
    }

    @Override
    public org.HdrHistogram.Histogram getNextHdrDeltaHistogram() {
        return this.hdrDeltaReservoir.getNextHdrHistogram();
    }

    @Override
    public Map<String, String> getLabels() {
        return labels;
    }
}

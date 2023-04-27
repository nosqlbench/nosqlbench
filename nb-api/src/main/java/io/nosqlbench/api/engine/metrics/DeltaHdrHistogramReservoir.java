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

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import io.nosqlbench.api.config.NBLabeledElement;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;
import org.HdrHistogram.Recorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * A custom wrapping of snapshotting logic on the HdrHistogram. This histogram will always report the last histogram
 * since it was most recently asked for with the getDeltaSnapshot(...) method.
 * This provides local snapshot timing, but with a consistent view for reporting channels about what those snapshots
 * most recently looked like.
 *
 * <p>This implementation also supports attaching a single log writer. If a log writer is attached, each
 * time an interval is snapshotted internally, the data will also be written to an hdr log via the writer.</p>
 */
public final class DeltaHdrHistogramReservoir implements Reservoir, NBLabeledElement {
    private static final Logger logger = LogManager.getLogger(DeltaHdrHistogramReservoir.class);

    private final Recorder recorder;
    private Histogram lastHistogram;

    private Histogram intervalHistogram;
    private long intervalHistogramEndTime = System.currentTimeMillis();
    private final Map<String, String> labels;
    private HistogramLogWriter writer;

    /**
     * Create a reservoir with a default recorder. This recorder should be suitable for most usage.
     *
     * @param labels              the labels to give to the reservoir, for logging purposes
     * @param significantDigits how many significant digits to track in the reservoir
     */
    public DeltaHdrHistogramReservoir(final Map<String, String> labels, final int significantDigits) {
        this.labels = labels;
        recorder = new Recorder(significantDigits);

        /*
         * Start by flipping the recorder's interval histogram.
         * - it starts our counting at zero. Arguably this might be a bad thing if you wanted to feed in
         *   a recorder that already had some measurements? But that seems crazy.
         * - intervalHistogram can be nonnull.
         * - it lets us figure out the number of significant digits to use in runningTotals.
         */
        this.intervalHistogram = this.recorder.getIntervalHistogram();
        this.lastHistogram = new Histogram(this.intervalHistogram.getNumberOfSignificantValueDigits());
    }

    @Override
    public int size() {
        // This appears to be infrequently called, so not keeping a separate counter just for this.
        return this.getSnapshot().size();
    }

    @Override
    public void update(final long value) {
        this.recorder.recordValue(value);
    }

    /**
     * @return the data accumulated since the reservoir was created, or since the last call to this method
     */
    @Override
    public Snapshot getSnapshot() {
        this.lastHistogram = this.getNextHdrHistogram();
        return new DeltaHistogramSnapshot(this.lastHistogram);
    }

    public Histogram getNextHdrHistogram() {
        return this.getDataSinceLastSnapshotAndUpdate();
    }


    /**
     * @return last histogram snapshot that was provided by {@link #getSnapshot()}
     */
    public Snapshot getLastSnapshot() {
        return new DeltaHistogramSnapshot(this.lastHistogram);
    }

    /**
     * @return a copy of the accumulated state since the reservoir last had a snapshot
     */
    private synchronized Histogram getDataSinceLastSnapshotAndUpdate() {
        this.intervalHistogram = this.recorder.getIntervalHistogram(this.intervalHistogram);
        final long intervalHistogramStartTime = this.intervalHistogramEndTime;
        this.intervalHistogramEndTime = System.currentTimeMillis();

        this.intervalHistogram.setTag(labels.get("name"));
        this.intervalHistogram.setStartTimeStamp(intervalHistogramStartTime);
        this.intervalHistogram.setEndTimeStamp(this.intervalHistogramEndTime);

        this.lastHistogram = this.intervalHistogram.copy();
        this.lastHistogram.setTag(labels.get("name"));

        if (null != writer) this.writer.outputIntervalHistogram(this.lastHistogram);
        return this.lastHistogram;
    }

    /**
     * Write the last results via the log writer.
     *
     * @param writer the log writer to use
     */
    public void write(final HistogramLogWriter writer) {
        writer.outputIntervalHistogram(this.lastHistogram);
    }

    public DeltaHdrHistogramReservoir copySettings() {
        return new DeltaHdrHistogramReservoir(labels, this.intervalHistogram.getNumberOfSignificantValueDigits());
    }

    public void attachLogWriter(final HistogramLogWriter logWriter) {
        writer = logWriter;
    }

    public Histogram getLastHistogram() {
        return this.lastHistogram;
    }

    @Override
    public Map<String, String> getLabels() {
        return labels;
    }
}

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

package io.nosqlbench.api.engine.metrics;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;
import org.HdrHistogram.Recorder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A custom wrapping of snapshotting logic on the HdrHistogram. This histogram will always report the last histogram
 * since it was most recently asked for with the getDeltaSnapshot(...) method.
 * This provides local snapshot timing, but with a consistent view for reporting channels about what those snapshots
 * most recently looked like.
 *
 * <p>This implementation also supports attaching a single log writer. If a log writer is attached, each
 * time an interval is snapshotted internally, the data will also be written to an hdr log via the writer.</p>
 *
 */
public final class DeltaHdrHistogramReservoir implements Reservoir {
    private final static Logger logger = LogManager.getLogger(DeltaHdrHistogramReservoir.class);

    private final Recorder recorder;
    private Histogram lastHistogram;

    private Histogram intervalHistogram;
    private long intervalHistogramEndTime = System.currentTimeMillis();
    private final String metricName;
    private HistogramLogWriter writer;

    /**
     * Create a reservoir with a default recorder. This recorder should be suitable for most usage.
     *
     * @param name the name to give to the reservoir, for logging purposes
     * @param significantDigits how many significant digits to track in the reservoir
     */
    public DeltaHdrHistogramReservoir(String name, int significantDigits) {
        this.metricName = name;
        this.recorder = new Recorder(significantDigits);

        /*
         * Start by flipping the recorder's interval histogram.
         * - it starts our counting at zero. Arguably this might be a bad thing if you wanted to feed in
         *   a recorder that already had some measurements? But that seems crazy.
         * - intervalHistogram can be nonnull.
         * - it lets us figure out the number of significant digits to use in runningTotals.
         */
        intervalHistogram = recorder.getIntervalHistogram();
        lastHistogram = new Histogram(intervalHistogram.getNumberOfSignificantValueDigits());
    }

    @Override
    public int size() {
        // This appears to be infrequently called, so not keeping a separate counter just for this.
        return getSnapshot().size();
    }

    @Override
    public void update(long value) {
        recorder.recordValue(value);
    }

    /**
     * @return the data accumulated since the reservoir was created, or since the last call to this method
     */
    @Override
    public Snapshot getSnapshot() {
        lastHistogram = getNextHdrHistogram();
        return new DeltaHistogramSnapshot(lastHistogram);
    }

    public Histogram getNextHdrHistogram() {
        return getDataSinceLastSnapshotAndUpdate();
    }


    /**
     * @return last histogram snapshot that was provided by {@link #getSnapshot()}
     */
    public Snapshot getLastSnapshot() {
        return new DeltaHistogramSnapshot(lastHistogram);
    }

    /**
     * @return a copy of the accumulated state since the reservoir last had a snapshot
     */
    private synchronized Histogram getDataSinceLastSnapshotAndUpdate() {
        intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);
        long intervalHistogramStartTime = intervalHistogramEndTime;
        intervalHistogramEndTime = System.currentTimeMillis();

        intervalHistogram.setTag(metricName);
        intervalHistogram.setStartTimeStamp(intervalHistogramStartTime);
        intervalHistogram.setEndTimeStamp(intervalHistogramEndTime);

        lastHistogram = intervalHistogram.copy();
        lastHistogram.setTag(metricName);

        if (writer!=null) {
            writer.outputIntervalHistogram(lastHistogram);
        }
        return lastHistogram;
    }

    /**
     * Write the last results via the log writer.
     * @param writer the log writer to use
     */
    public void write(HistogramLogWriter writer) {
        writer.outputIntervalHistogram(lastHistogram);
    }

    public DeltaHdrHistogramReservoir copySettings() {
        return new DeltaHdrHistogramReservoir(this.metricName, intervalHistogram.getNumberOfSignificantValueDigits());
    }

    public void attachLogWriter(HistogramLogWriter logWriter) {
        this.writer = logWriter;
    }

    public Histogram getLastHistogram() {
        return lastHistogram;
    }
}

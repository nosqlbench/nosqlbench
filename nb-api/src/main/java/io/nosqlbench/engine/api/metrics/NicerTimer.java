/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Timer;
import org.HdrHistogram.Histogram;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class NicerTimer extends Timer implements DeltaSnapshotter, HdrDeltaHistogramAttachment, TimerAttachment {
    private final String metricName;
    private final DeltaHdrHistogramReservoir deltaHdrHistogramReservoir;
    private long cacheExpiry = 0L;
    private List<Timer> mirrors;

    public NicerTimer(String metricName, DeltaHdrHistogramReservoir deltaHdrHistogramReservoir) {
        super(deltaHdrHistogramReservoir);
        this.metricName = metricName;
        this.deltaHdrHistogramReservoir = deltaHdrHistogramReservoir;
    }

    @Override
    public ConvenientSnapshot getSnapshot() {
        if (System.currentTimeMillis() >= cacheExpiry) {
            return new ConvenientSnapshot(deltaHdrHistogramReservoir.getSnapshot());
        } else {
            return new ConvenientSnapshot(deltaHdrHistogramReservoir.getLastSnapshot());
        }
    }

    public DeltaSnapshotReader getDeltaReader() {
        return new DeltaSnapshotReader(this);
    }

    @Override
    public ConvenientSnapshot getDeltaSnapshot(long cacheTimeMillis) {
        this.cacheExpiry = System.currentTimeMillis() + cacheTimeMillis;
        return new ConvenientSnapshot(deltaHdrHistogramReservoir.getSnapshot());
    }

    @Override
    public synchronized NicerTimer attachHdrDeltaHistogram() {
        if (mirrors==null) {
            mirrors = new CopyOnWriteArrayList<>();
        }
        DeltaHdrHistogramReservoir sameConfigReservoir = this.deltaHdrHistogramReservoir.copySettings();
        NicerTimer mirror = new NicerTimer(this.metricName, sameConfigReservoir);
        mirrors.add(mirror);
        return mirror;
    }
    @Override
    public Timer attachTimer(Timer timer) {
        if (mirrors==null) {
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
        if (mirrors!=null) {
            for (Timer mirror : mirrors) {
                mirror.update(duration,unit);
            }
        }
    }

}

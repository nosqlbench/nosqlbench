package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.PrefetchPlan;

/// Console meter for a binding-level prefetch: the plan's cost is
/// announced before any bytes move, progress is emitted during the
/// download (rate-limited), and completion is confirmed. Lines go to
/// stderr so they are visible regardless of logging configuration —
/// this is a download meter, not a log event. Silent when the plan has
/// nothing to fetch, so local and already-warm facets add no noise.
final class PrefetchMeter implements PrebufferProgress {

    private static final long INTERVAL_NANOS = 2_000_000_000L;
    private final String label;
    private final long planned;
    private volatile long lastEmit;

    PrefetchMeter(String label, PrefetchPlan plan) {
        this.label = label;
        this.planned = plan.bytesToFetch();
        if (planned > 0)
            System.err.printf("[vectordata] %s: fetching %s in %d range(s)%n", label, bytes(planned), plan.requests());
    }

    /// Per-range progress from the blocking prefetch path.
    @Override public void onProgress(long cachedBytes, long totalBytes) {
        if (planned == 0) return;
        long now = System.nanoTime();
        if (now - lastEmit < INTERVAL_NANOS) return;
        lastEmit = now;
        System.err.printf("[vectordata] %s: %s / %s%n", label, bytes(cachedBytes), bytes(totalBytes));
    }

    /// Confirms a completed blocking fetch.
    void complete() {
        if (planned > 0) System.err.printf("[vectordata] %s: fetch complete (%s)%n", label, bytes(planned));
    }

    /// Follows a background prefetch on a daemon thread, emitting the
    /// handle's byte counter until the worker finishes or is cancelled.
    void watch(PrefetchHandle handle) {
        if (planned == 0) return;
        Thread watcher = new Thread(() -> {
            while (!handle.isDone()) {
                System.err.printf("[vectordata] %s: %s fetched of %s%n", label, bytes(handle.bytesFetched()), bytes(planned));
                try { Thread.sleep(2000); } catch (InterruptedException interrupted) { return; }
            }
            System.err.printf("[vectordata] %s: background fetch done (%s of %s)%n",
                label, bytes(handle.bytesFetched()), bytes(planned));
        }, "prefetch-progress:" + label);
        watcher.setDaemon(true);
        watcher.start();
    }

    static String bytes(long count) {
        if (count >= 1L << 30) return String.format("%.1f GiB", count / (double) (1L << 30));
        if (count >= 1L << 20) return String.format("%.1f MiB", count / (double) (1L << 20));
        if (count >= 1L << 10) return String.format("%.1f KiB", count / (double) (1L << 10));
        return count + " B";
    }
}

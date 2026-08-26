/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/// A prefetch running on another thread. Returned by
/// [TestDataView#prefetchInBackground].
///
/// The plan is computed synchronously, *before* the handle exists, so a
/// caller learns the cost up front and can decide not to proceed; only
/// the fetching runs off-thread. Discarding the handle detaches: the
/// fetch keeps running on a daemon thread and its bytes land in the
/// cache, which is what a caller who has moved on still wants. A
/// failure is logged by the worker whether or not anybody joins, so an
/// unwatched prefetch cannot fail silently.
public final class PrefetchHandle {
    private static final System.Logger LOGGER = System.getLogger(PrefetchHandle.class.getName());

    /// Worker-facing view of the handle: cancellation polling and
    /// progress counters. Passed to the [FetchBody] supplied by the
    /// [TestDataView] implementation that launches the prefetch.
    public interface Ticker {
        /// Whether [PrefetchHandle#cancel] has been requested.
        boolean cancelled();
        /// Reports total bytes fetched so far across all ranges.
        void bytes(long fetchedSoFar);
        /// Reports one issued range as complete.
        void rangeDone();
    }

    /// The fetch loop run on the background thread. Throwing records a
    /// failure that [#join] rethrows.
    @FunctionalInterface
    public interface FetchBody { void fetch(Ticker ticker) throws Exception; }

    private final PrefetchPlan plan;
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean done = new AtomicBoolean();
    private final AtomicLong bytesFetched = new AtomicLong();
    private final AtomicInteger rangesFetched = new AtomicInteger();
    private volatile String error;
    private final Thread thread;

    private PrefetchHandle(PrefetchPlan plan, Thread thread) { this.plan = plan; this.thread = thread; }

    /// Starts the worker thread for an already-planned prefetch.
    /// Intended for [TestDataView] implementations rather than end
    /// users, who receive a handle from
    /// [TestDataView#prefetchInBackground].
    public static PrefetchHandle launch(String facet, PrefetchPlan plan, FetchBody body) {
        PrefetchHandle[] slot = new PrefetchHandle[1];
        Thread worker = new Thread(() -> {
            PrefetchHandle handle = slot[0];
            try {
                body.fetch(new Ticker() {
                    @Override public boolean cancelled() { return handle.cancelled.get(); }
                    @Override public void bytes(long fetchedSoFar) { handle.bytesFetched.set(fetchedSoFar); }
                    @Override public void rangeDone() { handle.rangesFetched.incrementAndGet(); }
                });
            } catch (Throwable failure) {
                handle.error = failure.toString();
                LOGGER.log(System.Logger.Level.WARNING, "prefetch of '" + facet + "' failed: " + failure);
            } finally {
                handle.done.set(true);
            }
        }, "prefetch:" + facet);
        worker.setDaemon(true);
        PrefetchHandle handle = new PrefetchHandle(plan, worker);
        slot[0] = handle;
        worker.start();
        return handle;
    }

    /// What this prefetch set out to do. Available immediately.
    public PrefetchPlan plan() { return plan; }

    /// Whether the worker has finished — successfully, in error, or by
    /// cancellation.
    public boolean isDone() { return done.get(); }

    /// Bytes fetched so far.
    public long bytesFetched() { return bytesFetched.get(); }

    /// Ranges completed so far, of [PrefetchPlan#requests].
    public int rangesFetched() { return rangesFetched.get(); }

    /// Asks the worker to stop. Granular to a range, not to a byte: a
    /// fetch already in flight runs to completion, because the
    /// transport cannot abandon one part-way and leave the chunk state
    /// honest. Ranges already fetched stay in the cache — a cancelled
    /// prefetch is partial work, not undone work.
    public void cancel() { cancelled.set(true); }

    public boolean isCancelled() { return cancelled.get(); }

    /// Waits for the fetch and reports what it did. A worker failure
    /// surfaces here as a [VectorDataException]; joining a cancelled
    /// prefetch is not an error, because stopping early is what was
    /// asked for.
    public PrefetchReport join() {
        try { thread.join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new VectorDataException("Interrupted while joining prefetch", e); }
        if (error != null) throw new VectorDataException(error);
        return new PrefetchReport(plan, rangesFetched.get());
    }
}

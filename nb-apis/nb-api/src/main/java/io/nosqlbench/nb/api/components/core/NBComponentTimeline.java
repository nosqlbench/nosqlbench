/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.components.core;

import java.util.concurrent.TimeUnit;

public interface NBComponentTimeline {

    NBInvokableState getComponentState();
    /**
     * This will be 0L if the component hasn't fully started. When non-zero, it represents
     * the raw {@link System#nanoTime()} sample captured at construction time. Consumers should
     * derive durations by subtracting against this value rather than treating it as an absolute
     * timestamp.
     * @return nanosecond time of component construction
     */
    long nanosof_start();

    /**
     * This will be 0L if the component hasn't began the process of closing down. When non-zero it
     * represents a monotonic timestamp from {@link System#nanoTime()} and is only meaningful when
     * compared to {@link #nanosof_start()}.
     * @return nanosecond time of invoking {@link NBBaseComponent#close()}
     */
    long nanosof_close();

    /**
     * This will be 0L if the component hasn't completed teardown. Otherwise it will be the
     * {@link System#nanoTime()} sample captured at the end of teardown logic. Treat this as a
     * monotonic timestamp suitable for duration calculations only.
     * For this reason, it is imperative that any overrides to {@link NBBaseComponent#teardown()}
     * are called, and called last in the overridden teardown method.
     * @return nanosecond time of teardown completion
     */
    long nanosof_teardown();

    /**
     * This will be 0L if the component hasn't logged an error. Otherwise it will be the
     * {@link System#nanoTime()} sample captured when the error was reported. Treat it as
     * monotonic and use for relative calculations only.
     * @return nanosecond time of the error
     */
    long nanosof_error();

    long started_epoch_ms();

    /**
     * Compute the amount of time, in nanoseconds, that the component has been active based on its
     * lifecycle state and stored monotonic timestamps.
     */
    default long uptimeNanos() {
        long start = nanosof_start();
        if (start == 0L) {
            return 0L;
        }
        long delta = switch (getComponentState()) {
            case ERRORED -> nanosof_error() - start;
            case STARTING, RUNNING -> System.nanoTime() - start;
            case CLOSING -> nanosof_close() - start;
            case STOPPED -> nanosof_teardown() - start;
        };
        return Math.max(0L, delta);
    }

    /**
     * Convenience method returning {@link #uptimeNanos()} converted to milliseconds.
     */
    default long uptimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(uptimeNanos());
    }
}

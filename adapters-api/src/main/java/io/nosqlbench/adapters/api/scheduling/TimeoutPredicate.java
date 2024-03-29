/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapters.api.scheduling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TimeoutPredicate<T> {
    private final static Logger logger = LogManager.getLogger(TimeoutPredicate.class);

    private final Supplier<T> source;
    private final Predicate<T> predicate;
    private final long timeoutNanos;
    private final long blockingNanos;
    private final boolean rethrow;
    private long pulseTime = 0L;
    private long startNanos;
    private long endNanos;

    public static <PT> TimeoutPredicate<PT> of(
        Predicate<PT> o,
        Duration timeout,
        Duration interval,
        boolean b
    ) {
        return new TimeoutPredicate<>(o, timeout, interval, b);
    }

    public static <PT> TimeoutPredicate<PT> of(
        Supplier<PT> source, Predicate<PT> predicate, Duration timeout, Duration interval, boolean rethrow
    ) {
        return new TimeoutPredicate<>(source, predicate, timeout, interval, rethrow);
    }

    private TimeoutPredicate(
        Predicate<T> predicate,
        Duration timeout,
        Duration minBlockingInterval,
        boolean rethrow
    ) {
        this(null, predicate, timeout, minBlockingInterval, rethrow);
    }

    private TimeoutPredicate(
        Supplier<T> source,
        Predicate<T> predicate,
        Duration timeout,
        Duration minBlockingInterval,
        boolean rethrow
    ) {
        this.source = source;
        this.predicate = Objects.requireNonNull(predicate);

        timeoutNanos = Objects.requireNonNull(timeout).toNanos();
        blockingNanos = Objects.requireNonNull(minBlockingInterval).toNanos();
        startNanos = System.nanoTime();
        endNanos = startNanos + timeoutNanos;
        this.rethrow = rethrow;
    }


    public Result<T> test(T value) {
        long totalNanos = blockUntilNextInterval();

        boolean isComplete = false;
        try {
            isComplete = predicate.test(value);
            long remaining = endNanos - pulseTime;
            if (isComplete) {
                return new Result<>(value, Status.complete, totalNanos, timeoutNanos, null);
            } else if (remaining > 0) {
//                System.out.println("pulse:" + pulseTime + " end:" + endNanos + " remaining:" + remaining);
                return new Result<>(value, Status.pending, totalNanos, timeoutNanos, null);
            } else {
//                System.out.println("pulse:" + pulseTime + " end:" + endNanos + " remaining:" + remaining);
                return new Result<>(value, Status.incomplete, totalNanos, timeoutNanos, null);
            }
        } catch (Exception e) {
            logger.error("exception caught while evaluating timeout predicate:" + e, e);
            if (rethrow) throw new RuntimeException(e);
            return new Result<>(value, Status.error, totalNanos, timeoutNanos, new RuntimeException(e));
        }
    }

    public Result<T> test() {
        Objects.requireNonNull(source);
        T value = source.get();
        return test(value);
    }

    public long blockUntilNextInterval() {
        if (pulseTime == 0L) { // first try has no delay
            pulseTime = System.nanoTime();
            return 0L;
        }

        long now = System.nanoTime();
        long targetNanos = Math.max(now, Math.min(endNanos, pulseTime + blockingNanos));
        while (now <= targetNanos) {
            LockSupport.parkNanos(targetNanos - now);
            now = System.nanoTime();
        }
        pulseTime = now;
        long currentTime = pulseTime - startNanos;
        return currentTime;
    }

    public static enum Status {
        complete,
        pending,
        incomplete,
        error
    }

    public static record Result<T>(
        T value,
        Status status,
        long duration_ns,
        long timeout_ns,
        RuntimeException exception
    ) {
        public String timeSummary() {
            return statusDuration() + " / "
                + timeoutDuration();
        }

        public Duration statusDuration() {
            return Duration.of(duration_ns - (duration_ns % 1_000_000), ChronoUnit.NANOS);
        }

        public Duration timeoutDuration() {
            return Duration.of(timeout_ns, ChronoUnit.NANOS);
        }

        public boolean isComplete() {
            return status==Status.complete;
        }
        public boolean isIncomplete() {
            return status==Status.incomplete;
        }
        public boolean isPending() {
            return status==Status.pending;
        }
    }

    @Override
    public String toString() {
        return "timeout:" + Duration.of(this.endNanos - this.startNanos, ChronoUnit.NANOS)
            + ", current:" + Duration.of((this.endNanos - this.pulseTime), ChronoUnit.NANOS)
            + ", interval:" + Duration.of(this.blockingNanos, ChronoUnit.NANOS);
    }
}

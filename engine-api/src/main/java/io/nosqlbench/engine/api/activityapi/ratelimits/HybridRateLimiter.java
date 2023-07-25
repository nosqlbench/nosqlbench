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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import com.codahale.metrics.Gauge;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <H2>Synopsis</H2>
 * <p>This rate limiter uses nanoseconds as the unit of timing. This
 * works well because it is the native precision of the system timer
 * interface via {@link System#nanoTime()}. It is also low-error
 * in terms of rounding between floating point rates and nanoseconds,
 * at least in the round numbers that users tend to use. Further,
 * the current scheduling state is maintained as an atomic view of
 * accumulated nanoseconds granted to callers -- referred to here as the
 * ticks accumulator. This further simplifies the implementation by
 * allowing direct comparison of scheduled times with the current
 * state of the high-resolution system timer.
 *
 * <H2>Design Notes</H2>
 * This implementation makes certain trade-offs needed to support a combination of requirements.
 * Specifically, some small degree of inaccuracy is allowed to enable higher throughput when
 * needed. Some practical limitations affect how accurate we can be:
 *
 * <OL>
 * <LI>This is not a real-time system with guarantees on scheduling.</LI>
 * <LI>Calling overhead is significant for reading the RTC or sleeping.</LI>
 * <LI>Controlling the accuracy of a delay is not possible under any level of load.</LI>
 * <LI>It is undesirable (wasteful) to use spin loops to delay.</LI>
 * </OL>
 *
 * Together, these factors mean a compromise is inevitable. In practice it means that a very accurate
 * implementation will likely be very slow, and a very fast implementation will likely be very inaccurate.
 * This implementation tries to strike a balance, providing accuracy near the microsecond level,
 * while allowing rates in the tens of millions per second, even under heavy thread contention.
 *
 * <H2>Burst Ratio</H2>
 * <p>
 * This rate limiter provides a sliding scale between strict rate limiting and average rate limiting,
 * the difference between the two controlled by a <em>burst ratio</em> parameter. When the burst
 * ratio is 1.0, the rate limiter acts as a strict rate limiter, disallowing faster operations
 * from using time that was previously forfeited by prior slower operations. This is a "use it
 * or lose it" mode that means things like GC events can steal throughput from a running client
 * as a necessary effect of losing time in a strict timing sense.
 * </p>
 *
 * <p>
 * When the burst ratio is set to higher than 1.0, faster operations may recover lost time from
 * previously slower operations. This means that any valleys created in the actual op rate of the
 * client can be converted into plateaus of throughput above the strict rate, but only at a speed that
 * fits within (op rate * burst ratio). This allows for workloads to approximate the average
 * target rate over time, with controllable bursting rates. This ability allows for near-strict
 * behavior while allowing clients to still track truer to rate limit expectations, so long as the
 * overall workload is not saturating resources.
 * </p>
 */
@Service(value = RateLimiter.class, selector = "hybrid")
public class HybridRateLimiter implements RateLimiter {

    private static final Logger logger = LogManager.getLogger(HybridRateLimiter.class);
    private NBLabeledElement named;

    //private volatile TokenFiller filler;
    private volatile long starttime;

    // rate controls
    private RateSpec rateSpec;

    // basic state
    private String label;
    private State state = State.Idle;
    // metrics
    private Gauge<Long> delayGauge;
    private Gauge<Double> avgRateGauge;
    private Gauge<Double> burstRateGauge;
    private TokenPool tokens;
    // diagnostics

    // TODO Doc rate limiter scenarios, including when you want to reset the waittime, and when you don't
    private final AtomicLong cumulativeWaitTimeNanos = new AtomicLong(0L);

    protected HybridRateLimiter() {
    }

    public HybridRateLimiter(final NBLabeledElement named, final String label, final RateSpec rateSpec) {
        this.label = label;
        this.init(named);
        this.named = named;
        applyRateSpec(rateSpec);
    }

    protected void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public long maybeWaitForOp() {
        return this.tokens.blockAndTake();
    }

    @Override
    public long getTotalWaitTime() {
        return cumulativeWaitTimeNanos.get() + this.getWaitTime();
    }

    @Override
    public long getWaitTime() {
        return this.tokens.getWaitTime();
    }

    @Override
    public RateSpec getRateSpec() {
        return rateSpec;
    }

    @Override
    public synchronized void applyRateSpec(final RateSpec updatingRateSpec) {

        if (null == updatingRateSpec) throw new RuntimeException("RateSpec must be defined");

        if (updatingRateSpec.equals(rateSpec) && !updatingRateSpec.isRestart()) return;

        rateSpec = updatingRateSpec;
        tokens = null == this.tokens ? new ThreadDrivenTokenPool(this.rateSpec, this.named) : tokens.apply(this.named, this.rateSpec);
//        this.filler = (this.filler == null) ? new TokenFiller(rateSpec, activityDef) : filler.apply(rateSpec);
//        this.tokens = this.filler.getTokenPool();

        if ((State.Idle == this.state) && updatingRateSpec.isAutoStart()) start();
        else if (updatingRateSpec.isRestart()) restart();
    }


    protected void init(final NBLabeledElement activityDef) {
        delayGauge = ActivityMetrics.gauge(activityDef, this.label + ".waittime", new RateLimiters.WaitTimeGauge(this));
        avgRateGauge = ActivityMetrics.gauge(activityDef, this.label + ".config.cyclerate", new RateLimiters.RateGauge(this));
        burstRateGauge = ActivityMetrics.gauge(activityDef, this.label + ".config.burstrate", new RateLimiters.BurstRateGauge(this));
    }

    @Override
    public synchronized void start() {

        switch (this.state) {
            case Started:
//                logger.warn("Tried to start a rate limiter that was already started. If this is desired, use restart() instead");
                // TODO: Find a better way to warn about spurious rate limiter
                // starts, since the check condition was not properly isolated
                break;
            case Idle:
                final long nanos = this.getNanoClockTime();
                starttime = nanos;
                tokens.start();
                this.state = State.Started;
                break;
        }
    }

    public synchronized long restart() {
        switch (this.state) {
            case Idle:
                start();
                return 0L;
            case Started:
                final long accumulatedWaitSinceLastStart = this.cumulativeWaitTimeNanos.get();
                this.cumulativeWaitTimeNanos.set(0L);
                return tokens.restart() + accumulatedWaitSinceLastStart;
            default:
                return 0L;
        }
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    private synchronized void checkpointCumulativeWaitTime() {
        final long nanos = this.getNanoClockTime();
        starttime = nanos;
        this.cumulativeWaitTimeNanos.addAndGet(this.getWaitTime());
    }

    protected long getNanoClockTime() {
        return System.nanoTime();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(HybridRateLimiter.class.getSimpleName());
        sb.append("{\n");
        if (null != this.getRateSpec()) sb.append("      spec:").append(rateSpec.toString());
        if (null != this.tokens) sb.append(",\n tokenpool:").append(tokens);
        if (null != this.state) sb.append(",\n     state:'").append(state).append('\'');
        sb.append("\n}");
        return sb.toString();
    }

//    public String getRefillLog() {
//        return this.filler.getRefillLog();
//    }

    private enum State {
        Idle,
        Started
    }

    private class PoolGauge implements Gauge<Long> {
        private final HybridRateLimiter rl;

        public PoolGauge(final HybridRateLimiter hybridRateLimiter) {
            rl = hybridRateLimiter;
        }

        @Override
        public Long getValue() {
            final TokenPool pool = this.rl.tokens;
            if (null == pool) return 0L;
            return pool.getWaitTime();
        }
    }
}

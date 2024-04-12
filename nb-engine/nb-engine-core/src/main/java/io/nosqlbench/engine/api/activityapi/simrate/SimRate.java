/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.simrate;

import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * <H2>Invariants</H2>
 * <UL>
 * <LI>When filler is defined, the pool is being replenished, and the rate limiter is active.</LI>
 * <LI>Any changes to filler state or filler actions must be guarded by the filler lock.</LI>
 * <LI>Summary stats are accumulated when the filler is stopped.</LI>
 * <LI>State is initialized when the filler is started.</LI>
 * </UL>
 * <p>
 * In order to use {@link java.util.concurrent.Semaphore}, the canonical implementation which is designed to work
 * best with virtual threads, we have to scale time so that the token bucket will fit within 2^31.
 * To make this work across a range of rates from very slow to very fast, the resolution of time tracking has
 * to be set according to the rate specified.
 * <HR/>
 * <P>Explanation:</P>
 * <UL>
 * <LI>The time divisor in the rate is properly established as <EM>per interval</EM>. Conventionally, a
 * rate specified as "46Kops" is taken to mean "46Kops/s" or <EM>per second.</EM></LI>
 * <LI>The time which passes on the wall clock is the inverse of this, or in the example above,
 * <EM>1/46000</EM> of a second (21739 nanoseconds ideally).</LI>
 * <LI>At lower rates, like 0.01 ops/s, a number of seconds must pass with time accumulating into the token pool.
 * For 0.01/s, the number of nanoseconds representing a single op is 100_000_000_000, or more than 46 times
 * the value which is representable in a 32 bit semaphore.</LI>
 * <LI>By scaling the time unit, 0.01 ops/s can be represented as microseconds without losing significant timing
 * resolution with respect to the rate.</LI>
 * <LI>This scale factor works well to accommodate burst ratios up to 100%</LI>
 * </UL>
 */
public class SimRate extends NBBaseComponent implements RateLimiter, Thread.UncaughtExceptionHandler {
    private final static Logger logger = LogManager.getLogger(SimRate.class);
    private final Semaphore activePool = new Semaphore(0);
    private final AtomicLong waitingPool = new AtomicLong(0L);
    private Thread filler;
    private AtomicLong lastRefillAt = new AtomicLong(System.nanoTime());
    private boolean running = true;
    private long refillIntervalNanos = 1_000_000_0;
    private int maxActivePool, burstPoolSize, maxOverActivePool, ticksPerOp;
    private SimRateSpec spec;

    private LongAdder blocks = new LongAdder();

    private final ReentrantLock fillerLock = new ReentrantLock(false);

    private AtomicLong cumulativeWaitTimeTicks = new AtomicLong(0L);
    private long startTime;

    public SimRate(NBComponent parent, SimRateSpec spec) {
        super(parent, NBLabels.forKV().and("rateType",
            (spec instanceof CycleRateSpec? "cycle" : "stride")));
        this.spec = spec;
        initMetrics();
        startFiller();
    }

    private void initMetrics() {
        String rateType = getLabels().valueOf("rateType");
        create().gauge(
            rateType + "s_waittime",
            () -> (double) getWaitTimeDuration().get(ChronoUnit.NANOS),
            MetricCategory.Core,
            "The cumulative scheduling delay which accrues when" +
                " an activity is not able to execute operations as fast as requested."
        );
        create().gauge(
            "config_" + rateType + "rate",
            () -> spec.opsPerSec,
            MetricCategory.Config,
            "The configured cycle rate in ops/s"
        );
        create().gauge(
            rateType + "_config_burstrate",
            () -> spec.burstRatio,
            MetricCategory.Config,
            "the configured burst rate as a multiplier to the configured cycle rate. ex: 1.05 means 5% faster is allowed."
        );
    }

    public long refill() {
        try {

            fillerLock.lock();

            // checkpoint delta
            long now = System.nanoTime();
            long newNanoTokens = now - lastRefillAt.get();
            lastRefillAt.addAndGet(newNanoTokens);

            long intOverFlowNanoTokens = (newNanoTokens - Integer.MAX_VALUE);
            if (intOverFlowNanoTokens > 0) {
                waitingPool.addAndGet(spec.nanosToTicks(intOverFlowNanoTokens));
                newNanoTokens -= intOverFlowNanoTokens;
//                logger.warn(() -> "timer overflow with extra tokens=" + intOverFlowNanoTokens);
            }
            int newTokens = spec.nanosToTicks(newNanoTokens);

            // We need between 0 and the amount of space left in the active pool, but never negative
            final int needed = Math.max(this.maxActivePool - activePool.availablePermits(), 0);

            // We put at most how many tokens we have, but never more than we need
            final int allocatedToActivePool = Math.min(newTokens, needed);

            // Actually make the adjustment to the active pool
            this.activePool.release(allocatedToActivePool);

            // overflow logic
            // we have some tokens left over
            final long newTokensLeftOver = newTokens - allocatedToActivePool;
            // anything left over goes into the waiting pool
            this.waitingPool.addAndGet(newTokensLeftOver);

            // bursting logic (backfilling waiting pool into active pool)

            // we can move some of the waiting pool (lost time) tokens to the active pool
            // to provide bursting up to a limit, but the amount is normalized over time,
            // using the active pool capacity as the denominator. This means that 1/4 of a
            // second gets 1/4 of the burst and so on.

            // We only want to apply burst according to the amount of time we have relative
            // to how much time fits into one basic time unit.
            final double refillFactor = Math.min((double) newTokens / this.maxActivePool, 1.0D);

            int burstFillAllowed = (int) (refillFactor * this.burstPoolSize);

            burstFillAllowed = Math.min(this.maxOverActivePool - this.activePool.availablePermits(), burstFillAllowed);

            // we can only burst up to our burst limit, but only as much time as we have in the waiting pool already
            final int burstRecoveryToActivePool = (int) Math.max(0L,Math.min(burstFillAllowed, this.waitingPool.get()));

            this.waitingPool.addAndGet(-burstRecoveryToActivePool);
            this.activePool.release(burstRecoveryToActivePool);

//        System.out.print(this);
//        System.out.print(ANSI_BrightBlue + " adding=" + allocatedToActivePool);
//        if (0 < newTokensLeftOver)
//            System.out.print(ANSI_Red + " OVERFLOW:" + newTokensLeftOver + ANSI_Reset);
//        if (0 < burstFill) System.out.print(ANSI_BrightGreen + " BACKFILL:" + burstFill + ANSI_Reset);
//        if (intOverflowTokens>0) {
//            System.out.println(ANSI_BrightYellow+ "OVERFLOW:"+intOverflowTokens + ANSI_Reset);
//        }
//        System.out.println();

//            long waiting = this.activePool.availablePermits() + this.waitingPool.get();
//            return waiting;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            fillerLock.unlock();
            long waiting = this.activePool.availablePermits() + this.waitingPool.get();
            return waiting;
        }
    }


    @Override
    public void applyRateSpec(SimRateSpec updatingSimRateSpec) {
        logger.info("rate spec:\n" + updatingSimRateSpec);
        if (updatingSimRateSpec.getRate()==0d) {
            logger.warn("setting a rate of 0 will yield undefined results");
        }
        try {
            fillerLock.lock();

            if (null == updatingSimRateSpec) throw new RuntimeException("RateSpec must be defined");

            if (filler != null) {
                stopFiller();
            }
            this.spec = updatingSimRateSpec;
//            if (updatingSimRateSpec.verb == SimRateSpec.Verb.stop || updatingSimRateSpec.verb == SimRateSpec.Verb.restart) {
//                if (filler != null) {
//                    stopFiller();
//                }
//            }

//            convertTimeBase(spec, updatingSimRateSpec);
            initPools(spec);

            if (updatingSimRateSpec.verb == SimRateSpec.Verb.start || updatingSimRateSpec.verb == SimRateSpec.Verb.restart) {
                if (filler == null) {
                    startFiller();
                }
            }

        } finally {
            fillerLock.unlock();
        }

    }

    /**
     * When a rate limiter is stopped in the midst of a reconfiguration, carry over the accumulated time
     * in the active pool after converting the time base. Extra time that won't fit because of any time-base
     * scaling is sent into the waiting pool automatically.
     */
    private void convertTimeBase(SimRateSpec from, SimRateSpec to) {
        ChronoUnit fromUnit = from.unit;
        ChronoUnit toUnit = to.unit;

        if (fromUnit == toUnit) {
            return;
        }

        int drained = activePool.drainPermits();
        Duration drainedTime = Duration.of(drained, fromUnit);
        long totalNanos = (drainedTime.getSeconds() * 1_000_000_000) + drainedTime.getNano();

        int newTicks = to.nanosToTicks(totalNanos);
        int ticksForActive = Math.min(newTicks, 1_000_000_000);
        long nanosForActive = totalNanos - to.ticksToNanos(ticksForActive);
        long nanosForWaiting = totalNanos - nanosForActive;

        this.waitingPool.addAndGet(nanosForActive);
        this.activePool.release((int) nanosForWaiting);
    }

    private void accumulateStats() {
        this.cumulativeWaitTimeTicks.addAndGet(this.waitingPool.get());
    }

    @Override
    public Duration getWaitTimeDuration() {
        return Duration.of(waitingPool.get(), this.spec.unit);
    }

    @Override
    public double getWaitTimeSeconds() {
        Duration wait = getWaitTimeDuration();
        return (double) wait.getSeconds() + (wait.getNano() / 1_000_000_000d);
    }

    public void initPools(SimRateSpec simRateSpec) {
        maxActivePool = 1_000_000_000;
        maxOverActivePool = (int) (this.maxActivePool * simRateSpec.burstRatio());
        burstPoolSize = this.maxOverActivePool - this.maxActivePool;

        this.activePool.drainPermits();
        ticksPerOp = simRateSpec.ticksPerOp();
        this.activePool.release(ticksPerOp); // Allow the first op to start immediately, but only the first
        this.waitingPool.set(0);

        this.startTime = System.nanoTime();
    }

    public long block() {
        this.blocks.increment();
        try {
            this.activePool.acquire(ticksPerOp);
        } catch (InterruptedException ignored) {
        }
        return this.waitingPool.get() + this.activePool.availablePermits();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("SimRate filler thread threw an error, and will be stopped:" + e, e);
        stopFiller();
    }

    @Override
    public SimRateSpec getSpec() {
        return spec;
    }

    private void startFiller() {
        try {
            fillerLock.lock();
            initPools(spec);
            running = true;
            if (this.filler != null) {
                logger.debug("filler already started, no changes");
                return;
            }
            this.filler = new Thread(new FillerRunnable());
            filler.setName("FILLER");
            filler.setUncaughtExceptionHandler(this);
            filler.start();
        } finally {
            fillerLock.unlock();
        }
    }

    private void stopFiller() {
        try {
            fillerLock.lock();
            if (filler == null) {
                logger.debug("filler already stopped, no changes");
                return;
            }
            running = false;
            filler.join();
            filler = null;
            accumulateStats();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            fillerLock.unlock();
        }
    }


    private final class FillerRunnable implements Runnable {
        private long fills = 0L;
        private int charat = 0;

        @Override
        public void run() {
            while (running) {
                SimRate.this.refill();
                fills++;
                LockSupport.parkNanos(refillIntervalNanos);
            }
            logger.debug("shutting down refill thread");
        }
    }

    @Override
    public String toString() {
        return String.format(
            "{ rate:%f active:%d, max:%d, fill:'(%,3.1f%%)A (%,3.1f%%)B', wait_ns:%,d, blocks:%,d lock:%s ticks:%d}",
            this.spec.getRate(), this.activePool.availablePermits(), this.maxActivePool,
            (double) this.activePool.availablePermits() / this.maxActivePool * 100.0,
            (double) this.activePool.availablePermits() / this.maxOverActivePool * 100.0,
            this.waitingPool.get(),
            this.blocks.sum(),
            this.fillerLock.isLocked() ? "LOCKED" : "UNLOCKED", spec.ticksPerOp()
        );

    }

    public <U, V> Function<U, V> wrap(Function<U, V> f) {
        return new Wrapper<>(this, f);
    }

    public static class Wrapper<I, O> implements Function<I, O> {
        private final Function<I, O> function;
        private final SimRate ratelimiter;

        public Wrapper(SimRate ratelimiter, Function<I, O> function) {
            this.function = function;
            this.ratelimiter = ratelimiter;
        }

        @Override
        public O apply(I i) {
            ratelimiter.block();
            return function.apply(i);
        }
    }

    @Override
    public Duration getTotalWaitTimeDuration() {
        Duration d1 = Duration.of(waitingPool.get(), this.spec.unit);
        Duration d2 = Duration.of(cumulativeWaitTimeTicks.get(), this.spec.unit);
        return d1.plus(d2);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }
}

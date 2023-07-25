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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.util.Colors;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <h2>Synopsis</h2>
 *
 * This TokenPool represents a finite quantity which can be
 * replenished with regular refills. Extra tokens that do not fit
 * within the active token pool are saved in a waiting token pool and
 * used to backfill when allowed according to the backfill rate.
 *
 * A detailed explanation for how this works will be included
 * at @link "http://docs.nosqlbench.io/" under dev notes.
 *
 * <p>This is the basis for the token-based rate limiters in
 * NB. This mechanism is easily adaptable to bursting
 * capability as well as a degree of stricter timing at speed.
 * Various methods for doing this in a lock free way were
 * investigated, but the intrinsic locks provided by synchronized
 * method won out for now. This may be revisited when EB is
 * retrofitted for J11.
 * </p>
 */
public class InlineTokenPool {

    private static final Logger logger = LogManager.getLogger(InlineTokenPool.class);

    public static final double MIN_CONCURRENT_OPS = 5;
    private final NBLabeledElement parentLabels;

    // Size limit of active pool
    private long maxActivePoolSize;
    // Size limit of burst pool incremental above active pool
    private long maxBurstPoolSize;
    // Size limit of total active tokens which can be waiting in active pool, considering burst
    private long maxActiveAndBurstSize;

    // Ratio of speed relative to base speed at which bursting is allowed
    private double burstRatio;

    // TODO Consider removing volatile after investigating

    // The active number of tokens (ns) available for consumers
    private volatile long activePool;
    // The tokens which were not claimed on time, and were moved into the waitime (reserve) pool
    private volatile long waitingPool;
    // How many tokens (ns) represent passage of time for a single op, given the op rate
    private long nanosPerOp;

    // The nanotime of the last refill
    private volatile long lastRefillAt;
    // metrics for refill
    private final Timer refillTimer;
    // update rate for refiller
    private final long interval = (long) 1.0E6;


    private RateSpec rateSpec;
//    private long debugTrigger=0L;
//    private long debugRate=1000000000;

    // Total number of thread blocks that occured since this token pool was started
    private long blocks;

    private final Lock lock = new ReentrantLock();
    private final Condition lockheld = this.lock.newCondition();

    /**
     * This constructor tries to pick reasonable defaults for the token pool for
     * a given rate spec. The active pool must be large enough to contain one
     * op worth of time, and the burst ratio
     *
     * @param rateSpec a {@link RateSpec}
     */
    public InlineTokenPool(final RateSpec rateSpec, final ActivityDef def, final NBLabeledElement parentLabels) {
        this.parentLabels = parentLabels;
        final ByteBuffer logbuf = this.getBuffer();
        this.apply(rateSpec);
        InlineTokenPool.logger.debug("initialized token pool: {} for rate:{}", this, rateSpec);
        refillTimer = ActivityMetrics.timer(parentLabels, "tokenfiller",4);
    }

    public InlineTokenPool(final long poolsize, final double burstRatio, final ActivityDef def, final NBLabeledElement parentLabels) {
        this.parentLabels = parentLabels;
        final ByteBuffer logbuf = this.getBuffer();
        maxActivePoolSize = poolsize;
        this.burstRatio = burstRatio;
        maxActiveAndBurstSize = (long) (this.maxActivePoolSize * burstRatio);
        maxBurstPoolSize = this.maxActiveAndBurstSize - this.maxActivePoolSize;
        refillTimer = ActivityMetrics.timer(parentLabels, "tokenfiller",4);
    }

    /**
     * Change the settings of this token pool, and wake any blocked callers
     * just in case it allows them to proceed.
     *
     * @param rateSpec The rate specifier.
     */
    public synchronized void apply(final RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        // maxActivePool is set to the higher of 1M or however many nanos are needed for 2 ops to be buffered
        maxActivePoolSize = Math.max((long) 1.0E6, (long) (rateSpec.getNanosPerOp() * InlineTokenPool.MIN_CONCURRENT_OPS));
        maxActiveAndBurstSize = (long) (this.maxActivePoolSize * rateSpec.getBurstRatio());
        burstRatio = rateSpec.getBurstRatio();

        maxBurstPoolSize = this.maxActiveAndBurstSize - this.maxActivePoolSize;
        nanosPerOp = rateSpec.getNanosPerOp();
        this.notifyAll();
    }


    public double getBurstRatio() {
        return this.burstRatio;
    }

    /**
     * Take tokens up to amt tokens form the pool and report
     * the amount of token removed.
     *
     * @param amt tokens requested
     * @return actual number of tokens removed, greater to or equal to zero
     */
    public synchronized long takeUpTo(final long amt) {
        final long take = Math.min(amt, this.activePool);
        this.activePool -= take;
        return take;
    }

    /**
     * wait for the given number of tokens to be available, and then remove
     * them from the pool.
     *
     * @return the total number of tokens untaken, including wait tokens
     */
    public long blockAndTake() {
        synchronized (this) {
            if (this.activePool >= this.nanosPerOp) {
                this.activePool -= this.nanosPerOp;
                return this.waitingPool + this.activePool;
            }
        }
        while (true) if (this.lock.tryLock()) try {
            while (this.activePool < this.nanosPerOp) this.dorefill();
            this.lockheld.signal();
            this.lockheld.signal();
        } finally {
            this.lock.unlock();
        }
        else try {
                this.lockheld.await();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
//        while (activePool < nanosPerOp) {
//            blocks++;
//            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
//            try {
//                wait();
////                wait(maxActivePoolSize / 1000000, (int) maxActivePoolSize % 1000000);
//            } catch (InterruptedException ignored) {
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
//        }
//        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);
//
//        activePool -= nanosPerOp;
//        return waitingPool + activePool;
    }

    public synchronized long blockAndTakeOps(final long ops) {
        final long totalNanosNeeded = ops * this.nanosPerOp;
        while (this.activePool < totalNanosNeeded) {
            this.blocks++;
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                this.wait();
//                wait(maxActivePoolSize / 1000000, (int) maxActivePoolSize % 1000000);
            } catch (final InterruptedException ignored) {
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        this.activePool -= totalNanosNeeded;
        return this.waitingPool + this.activePool;
    }

    public synchronized long blockAndTake(final long tokens) {
        //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
        //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        while (this.activePool < tokens) try {
            this.wait();
//                wait(maxActivePoolSize / 1000000, (int) maxActivePoolSize % 1000000);
        } catch (final InterruptedException ignored) {
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        this.activePool -= tokens;
        return this.waitingPool + this.activePool;
    }

    public long getWaitTime() {
        return this.activePool + this.waitingPool;
    }

    public long getWaitPool() {
        return this.waitingPool;
    }

    public long getActivePool() {
        return this.activePool;
    }

    /**
     * Add the given number of new tokens to the pool, forcing any amount
     * that would spill over the current pool size into the wait token pool, but
     * moving up to the configured burst tokens back from the wait token pool
     * otherwise.
     *
     * The amount of backfilling that occurs is controlled by the backfill ratio,
     * based on the number of tokens submitted. This causes normalizes the
     * backfilling rate to the fill rate, so that it is not sensitive to refill
     * scheduling.
     *
     * @param newTokens The number of new tokens to add to the token pools
     * @return the total number of tokens in all pools
     */
    public synchronized long refill(final long newTokens) {
        final boolean debugthis = false;
//        long debugAt = System.nanoTime();
//        if (debugAt>debugTrigger+debugRate) {
//            debugTrigger=debugAt;
//            debugthis=true;
//        }

        final long needed = Math.max(this.maxActivePoolSize - this.activePool, 0L);
        final long allocatedToActivePool = Math.min(newTokens, needed);
        this.activePool += allocatedToActivePool;


        // overflow logic
        final long allocatedToOverflowPool = newTokens - allocatedToActivePool;
        this.waitingPool += allocatedToOverflowPool;

        // backfill logic
        final double refillFactor = Math.min((double) newTokens / this.maxActivePoolSize, 1.0D);
        long burstFillAllowed = (long) (refillFactor * this.maxBurstPoolSize);

        burstFillAllowed = Math.min(this.maxActiveAndBurstSize - this.activePool, burstFillAllowed);
        final long burstFill = Math.min(burstFillAllowed, this.waitingPool);

        this.waitingPool -= burstFill;
        this.activePool += burstFill;

        if (debugthis) {
            System.out.print(this);
            System.out.print(Colors.ANSI_BrightBlue + " adding=" + allocatedToActivePool);
            if (0 < allocatedToOverflowPool)
                System.out.print(Colors.ANSI_Red + " OVERFLOW:" + allocatedToOverflowPool + Colors.ANSI_Reset);
            if (0 < burstFill) System.out.print(Colors.ANSI_BrightGreen + " BACKFILL:" + burstFill + Colors.ANSI_Reset);
            System.out.println();
        }

        //System.out.println(this);
        this.notifyAll();

        return this.activePool + this.waitingPool;
    }

    @Override
    public String toString() {
        return "Tokens: active=" + this.activePool + '/' + this.maxActivePoolSize
                + String.format(
                " (%3.1f%%)A (%3.1f%%)B ",
            (double) this.activePool / this.maxActivePoolSize * 100.0,
            (double) this.activePool / this.maxActiveAndBurstSize * 100.0) + " waiting=" + this.waitingPool +
                " blocks=" + this.blocks +
                " rateSpec:" + (null != rateSpec ? this.rateSpec.toString() : "NULL");
    }

    public RateSpec getRateSpec() {
        return this.rateSpec;
    }

    public synchronized long restart() {
        final long wait = this.activePool + this.waitingPool;
        this.activePool = 0L;
        this.waitingPool = 0L;
        return wait;
    }

    private ByteBuffer getBuffer() {
        RandomAccessFile image = null;
        try {
            image = new RandomAccessFile("tokenbucket.binlog", "rw");
            final ByteBuffer mbb = image.getChannel().map(MapMode.READ_WRITE, 0, image.length());
            return mbb;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void dorefill() {
        this.lastRefillAt = System.nanoTime();
        final long nextRefillTime = this.lastRefillAt + this.interval;
        long thisRefillTime = System.nanoTime();
        while (thisRefillTime < nextRefillTime) {
//            while (thisRefillTime < lastRefillAt + interval) {
            final long parkfor = Math.max(nextRefillTime - thisRefillTime, 0L);
            //System.out.println(ANSI_Blue + "parking for " + parkfor + "ns" + ANSI_Reset);
            LockSupport.parkNanos(parkfor);
            thisRefillTime = System.nanoTime();
        }

//            this.times[iteration]=thisRefillTime;
        final long delta = thisRefillTime - this.lastRefillAt;
//            this.amounts[iteration]=delta;
        this.lastRefillAt = thisRefillTime;

        //System.out.println(this);
        this.refill(delta);
        this.refillTimer.update(delta, TimeUnit.NANOSECONDS);
//            iteration++;

    }


}

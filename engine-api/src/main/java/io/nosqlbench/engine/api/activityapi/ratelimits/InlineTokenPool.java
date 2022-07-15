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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import com.codahale.metrics.Timer;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static io.nosqlbench.engine.api.util.Colors.*;

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

    private final static Logger logger = LogManager.getLogger(InlineTokenPool.class);

    public static final double MIN_CONCURRENT_OPS = 5;

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
    private final long interval = (long) 1E6;


    private RateSpec rateSpec;
//    private long debugTrigger=0L;
//    private long debugRate=1000000000;

    // Total number of thread blocks that occured since this token pool was started
    private long blocks = 0L;

    private final Lock lock = new ReentrantLock();
    private final Condition lockheld = lock.newCondition();

    /**
     * This constructor tries to pick reasonable defaults for the token pool for
     * a given rate spec. The active pool must be large enough to contain one
     * op worth of time, and the burst ratio
     *
     * @param rateSpec a {@link RateSpec}
     */
    public InlineTokenPool(RateSpec rateSpec, ActivityDef def) {
        ByteBuffer logbuf = getBuffer();
        apply(rateSpec);
        logger.debug("initialized token pool: " + this + " for rate:" + rateSpec);
        this.refillTimer = ActivityMetrics.timer(def, "tokenfiller",4);
    }

    public InlineTokenPool(long poolsize, double burstRatio, ActivityDef def) {
        ByteBuffer logbuf = getBuffer();
        this.maxActivePoolSize = poolsize;
        this.burstRatio = burstRatio;
        this.maxActiveAndBurstSize = (long) (maxActivePoolSize * burstRatio);
        this.maxBurstPoolSize = maxActiveAndBurstSize - maxActivePoolSize;
        this.refillTimer = ActivityMetrics.timer(def, "tokenfiller",4);
    }

    /**
     * Change the settings of this token pool, and wake any blocked callers
     * just in case it allows them to proceed.
     *
     * @param rateSpec The rate specifier.
     */
    public synchronized void apply(RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        // maxActivePool is set to the higher of 1M or however many nanos are needed for 2 ops to be buffered
        this.maxActivePoolSize = Math.max((long) 1E6, (long) ((double) rateSpec.getNanosPerOp() * MIN_CONCURRENT_OPS));
        this.maxActiveAndBurstSize = (long) (maxActivePoolSize * rateSpec.getBurstRatio());
        this.burstRatio = rateSpec.getBurstRatio();

        this.maxBurstPoolSize = maxActiveAndBurstSize - maxActivePoolSize;
        this.nanosPerOp = rateSpec.getNanosPerOp();
        notifyAll();
    }


    public double getBurstRatio() {
        return burstRatio;
    }

    /**
     * Take tokens up to amt tokens form the pool and report
     * the amount of token removed.
     *
     * @param amt tokens requested
     * @return actual number of tokens removed, greater to or equal to zero
     */
    public synchronized long takeUpTo(long amt) {
        long take = Math.min(amt, activePool);
        activePool -= take;
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
            if (activePool >= nanosPerOp) {
                activePool -= nanosPerOp;
                return waitingPool + activePool;
            }
        }
        while (true) {
            if (lock.tryLock()) {
                try {
                    while (activePool < nanosPerOp) {
                        dorefill();
                    }
                    lockheld.signal();
                    lockheld.signal();
                } finally {
                    lock.unlock();
                }
            } else {
                try {
                    lockheld.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
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

    public synchronized long blockAndTakeOps(long ops) {
        long totalNanosNeeded = ops * nanosPerOp;
        while (activePool < totalNanosNeeded) {
            blocks++;
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                wait();
//                wait(maxActivePoolSize / 1000000, (int) maxActivePoolSize % 1000000);
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        activePool -= totalNanosNeeded;
        return waitingPool + activePool;
    }

    public synchronized long blockAndTake(long tokens) {
        while (activePool < tokens) {
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                wait();
//                wait(maxActivePoolSize / 1000000, (int) maxActivePoolSize % 1000000);
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        activePool -= tokens;
        return waitingPool + activePool;
    }

    public long getWaitTime() {
        return activePool + waitingPool;
    }

    public long getWaitPool() {
        return waitingPool;
    }

    public long getActivePool() {
        return activePool;
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
    public synchronized long refill(long newTokens) {
        boolean debugthis = false;
//        long debugAt = System.nanoTime();
//        if (debugAt>debugTrigger+debugRate) {
//            debugTrigger=debugAt;
//            debugthis=true;
//        }

        long needed = Math.max(maxActivePoolSize - activePool, 0L);
        long allocatedToActivePool = Math.min(newTokens, needed);
        activePool += allocatedToActivePool;


        // overflow logic
        long allocatedToOverflowPool = newTokens - allocatedToActivePool;
        waitingPool += allocatedToOverflowPool;

        // backfill logic
        double refillFactor = Math.min((double) newTokens / maxActivePoolSize, 1.0D);
        long burstFillAllowed = (long) (refillFactor * maxBurstPoolSize);

        burstFillAllowed = Math.min(maxActiveAndBurstSize - activePool, burstFillAllowed);
        long burstFill = Math.min(burstFillAllowed, waitingPool);

        waitingPool -= burstFill;
        activePool += burstFill;

        if (debugthis) {
            System.out.print(this);
            System.out.print(ANSI_BrightBlue + " adding=" + allocatedToActivePool);
            if (allocatedToOverflowPool > 0) {
                System.out.print(ANSI_Red + " OVERFLOW:" + allocatedToOverflowPool + ANSI_Reset);
            }
            if (burstFill > 0) {
                System.out.print(ANSI_BrightGreen + " BACKFILL:" + burstFill + ANSI_Reset);
            }
            System.out.println();
        }

        //System.out.println(this);
        notifyAll();

        return activePool + waitingPool;
    }

    @Override
    public String toString() {
        return "Tokens: active=" + activePool + "/" + maxActivePoolSize
                + String.format(
                " (%3.1f%%)A (%3.1f%%)B ",
                (((double) activePool / (double) maxActivePoolSize) * 100.0),
                (((double) activePool / (double) maxActiveAndBurstSize) * 100.0)) + " waiting=" + waitingPool +
                " blocks=" + blocks +
                " rateSpec:" + ((rateSpec != null) ? rateSpec.toString() : "NULL");
    }

    public RateSpec getRateSpec() {
        return rateSpec;
    }

    public synchronized long restart() {
        long wait = activePool + waitingPool;
        activePool = 0L;
        waitingPool = 0L;
        return wait;
    }

    private ByteBuffer getBuffer() {
        RandomAccessFile image = null;
        try {
            image = new RandomAccessFile("tokenbucket.binlog", "rw");
            ByteBuffer mbb = image.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, image.length());
            return mbb;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void dorefill() {
        lastRefillAt = System.nanoTime();
        long nextRefillTime = lastRefillAt + interval;
        long thisRefillTime = System.nanoTime();
        while (thisRefillTime < nextRefillTime) {
//            while (thisRefillTime < lastRefillAt + interval) {
            long parkfor = Math.max(nextRefillTime - thisRefillTime, 0L);
            //System.out.println(ANSI_Blue + "parking for " + parkfor + "ns" + ANSI_Reset);
            LockSupport.parkNanos(parkfor);
            thisRefillTime = System.nanoTime();
        }

//            this.times[iteration]=thisRefillTime;
        long delta = thisRefillTime - lastRefillAt;
//            this.amounts[iteration]=delta;
        lastRefillAt = thisRefillTime;

        //System.out.println(this);
        refill(delta);
        refillTimer.update(delta, TimeUnit.NANOSECONDS);
//            iteration++;

    }


}

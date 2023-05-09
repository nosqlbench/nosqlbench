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

import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
@Service(value= TokenPool.class, selector="threaded")
public class ThreadDrivenTokenPool implements TokenPool {

    private static final Logger logger = LogManager.getLogger(ThreadDrivenTokenPool.class);

    public static final double MIN_CONCURRENT_OPS = 2;

    private long maxActivePool;
    private long burstPoolSize;
    private long maxOverActivePool;
    private double burstRatio;
    // TODO Consider removing volatile after investigating
    private volatile long activePool;
    private volatile long waitingPool;
    private RateSpec rateSpec;
    private long nanosPerOp;
    private long blocks;

    private TokenFiller filler;

    /**
     * This constructor tries to pick reasonable defaults for the token pool for
     * a given rate spec. The active pool must be large enough to contain one
     * op worth of time, and the burst ratio
     *
     * @param rateSpec a {@link RateSpec}
     */
    public ThreadDrivenTokenPool(final RateSpec rateSpec, final NBLabeledElement named) {
        this.apply(named,rateSpec);
        ThreadDrivenTokenPool.logger.debug(() -> "initialized token pool: " + this + " for rate:" + rateSpec);
//        filler.start();
    }

    /**
     * Change the settings of this token pool, and wake any blocked callers
     * just in case it allows them to proceed.
     *
     * @param rateSpec The rate specifier.
     */
    @Override
    public synchronized TokenPool apply(final NBLabeledElement labeled, final RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        maxActivePool = Math.max((long) 1.0E6, (long) (rateSpec.getNanosPerOp() * ThreadDrivenTokenPool.MIN_CONCURRENT_OPS));
        maxOverActivePool = (long) (this.maxActivePool * rateSpec.getBurstRatio());
        burstRatio = rateSpec.getBurstRatio();

        burstPoolSize = this.maxOverActivePool - this.maxActivePool;
        nanosPerOp = rateSpec.getNanosPerOp();
        filler = null == this.filler ? new TokenFiller(rateSpec, this, labeled, 3) : this.filler.apply(rateSpec);
        this.notifyAll();
        return this;
    }


    @Override
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
    @Override
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
    @Override
    public synchronized long blockAndTake() {
        while (this.activePool < this.nanosPerOp) {
            this.blocks++;
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                this.wait(1000);
//                wait(maxActivePool / 1000000, 0);
            } catch (final InterruptedException ignored) {
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        this.activePool -= this.nanosPerOp;
        return this.waitingPool + this.activePool;
    }

    @Override
    public synchronized long blockAndTake(final long tokens) {
        //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
        //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        while (this.activePool < tokens) try {
            this.wait(this.maxActivePool / 1000000, (int) this.maxActivePool % 1000000);
        } catch (final InterruptedException ignored) {
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        this.activePool -= tokens;
        return this.waitingPool + this.activePool;
    }

    @Override
    public long getWaitTime() {
        return this.activePool + this.waitingPool;
    }

    @Override
    public long getWaitPool() {
        return this.waitingPool;
    }

    @Override
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

        final long needed = Math.max(this.maxActivePool - this.activePool, 0L);
        final long allocatedToActivePool = Math.min(newTokens, needed);
        this.activePool += allocatedToActivePool;


        // overflow logic
        final long allocatedToOverflowPool = newTokens - allocatedToActivePool;
        this.waitingPool += allocatedToOverflowPool;

        // backfill logic
        final double refillFactor = Math.min((double) newTokens / this.maxActivePool, 1.0D);
        long burstFillAllowed = (long) (refillFactor * this.burstPoolSize);

        burstFillAllowed = Math.min(this.maxOverActivePool - this.activePool, burstFillAllowed);
        final long burstFill = Math.min(burstFillAllowed, this.waitingPool);

        this.waitingPool -= burstFill;
        this.activePool += burstFill;

        if (debugthis) {
            System.out.print(this);
            System.out.print(ANSI_BrightBlue + " adding=" + allocatedToActivePool);
            if (0 < allocatedToOverflowPool)
                System.out.print(ANSI_Red + " OVERFLOW:" + allocatedToOverflowPool + ANSI_Reset);
            if (0 < burstFill) System.out.print(ANSI_BrightGreen + " BACKFILL:" + burstFill + ANSI_Reset);
            System.out.println();
        }
        //System.out.println(this);
        this.notifyAll();

        return this.activePool + this.waitingPool;
    }

    @Override
    public String toString() {
        return String.format(
            "{ active:%d, max:%d, fill:'(%,3.1f%%)A (%,3.1f%%)B', wait_ns:%,d, blocks:%,d }",
            this.activePool, this.maxActivePool,
            (double) this.activePool / this.maxActivePool * 100.0,
            (double) this.activePool / this.maxOverActivePool * 100.0,
            this.waitingPool,
            this.blocks
        );
    }

    @Override
    public RateSpec getRateSpec() {
        return this.rateSpec;
    }

    @Override
    public synchronized long restart() {
        final long wait = this.activePool + this.waitingPool;
        this.activePool = 0L;
        this.waitingPool = 0L;
        return wait;
    }

    @Override
    public synchronized void start() {
        this.filler.start();
    }
}

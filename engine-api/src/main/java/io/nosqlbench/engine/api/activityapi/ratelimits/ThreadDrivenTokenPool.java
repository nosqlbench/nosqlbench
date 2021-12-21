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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
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

    private final static Logger logger = LogManager.getLogger(ThreadDrivenTokenPool.class);

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
    //    private long debugTrigger=0L;
//    private long debugRate=1000000000;
    private long blocks = 0L;

    private TokenFiller filler;
    private final ActivityDef activityDef;

    /**
     * This constructor tries to pick reasonable defaults for the token pool for
     * a given rate spec. The active pool must be large enough to contain one
     * op worth of time, and the burst ratio
     *
     * @param rateSpec a {@link RateSpec}
     */
    public ThreadDrivenTokenPool(RateSpec rateSpec, ActivityDef activityDef) {
        this.activityDef = activityDef;
        apply(rateSpec);
        logger.debug("initialized token pool: " + this + " for rate:" + rateSpec);
//        filler.start();
    }

    /**
     * Change the settings of this token pool, and wake any blocked callers
     * just in case it allows them to proceed.
     *
     * @param rateSpec The rate specifier.
     */
    @Override
    public synchronized TokenPool apply(RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        this.maxActivePool = Math.max((long) 1E6, (long) ((double) rateSpec.getNanosPerOp() * MIN_CONCURRENT_OPS));
        this.maxOverActivePool = (long) (maxActivePool * rateSpec.getBurstRatio());
        this.burstRatio = rateSpec.getBurstRatio();

        this.burstPoolSize = maxOverActivePool - maxActivePool;
        this.nanosPerOp = rateSpec.getNanosPerOp();
        this.filler = (this.filler == null) ? new TokenFiller(rateSpec, this, activityDef) : filler.apply(rateSpec);
        notifyAll();
        return this;
    }


    @Override
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
    @Override
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
    @Override
    public synchronized long blockAndTake() {
        while (activePool < nanosPerOp) {
            blocks++;
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                wait(maxActivePool / 1000000, 0);
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println("waited for " + amt + "/" + activePool + " tokens");
        }
        //System.out.println(ANSI_BrightYellow + "taking " + amt + "/" + activePool + ANSI_Reset);

        activePool -= nanosPerOp;
        return waitingPool + activePool;
    }

    @Override
    public synchronized long blockAndTake(long tokens) {
        while (activePool < tokens) {
            //System.out.println(ANSI_BrightRed +  "waiting for " + amt + "/" + activePool + " of max " + maxActivePool + ANSI_Reset);
            try {
                wait(maxActivePool / 1000000, (int) maxActivePool % 1000000);
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

    @Override
    public long getWaitTime() {
        return activePool + waitingPool;
    }

    @Override
    public long getWaitPool() {
        return waitingPool;
    }

    @Override
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

        long needed = Math.max(maxActivePool - activePool, 0L);
        long allocatedToActivePool = Math.min(newTokens, needed);
        activePool += allocatedToActivePool;


        // overflow logic
        long allocatedToOverflowPool = newTokens - allocatedToActivePool;
        waitingPool += allocatedToOverflowPool;

        // backfill logic
        double refillFactor = Math.min((double) newTokens / maxActivePool, 1.0D);
        long burstFillAllowed = (long) (refillFactor * burstPoolSize);

        burstFillAllowed = Math.min(maxOverActivePool - activePool, burstFillAllowed);
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
        return String.format(
            "{ active:%d, max:%d, fill:'(%,3.1f%%)A (%,3.1f%%)B', wait_ns:%,d, blocks:%,d }",
            activePool, maxActivePool,
            (((double) activePool / (double) maxActivePool) * 100.0),
            (((double) activePool / (double) maxOverActivePool) * 100.0),
            waitingPool,
            blocks
        );
    }

    @Override
    public RateSpec getRateSpec() {
        return rateSpec;
    }

    @Override
    public synchronized long restart() {
        long wait = activePool + waitingPool;
        activePool = 0L;
        waitingPool = 0L;
        return wait;
    }

    @Override
    public synchronized void start() {
        filler.start();
    }
}

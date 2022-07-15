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
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TokenFiller implements Runnable {
    private final static Logger logger = LogManager.getLogger(TokenFiller.class);

    public final static double MIN_PER_SECOND = 10D;
    public final static double MAX_PER_SECOND = 1000D;
//    private final SysPerfData PERFDATA = SysPerf.get().getPerfData
//    (false);
    private final long interval = (long) 1E6;

    private final ThreadDrivenTokenPool tokenPool;
    private volatile boolean running = true;
    private RateSpec rateSpec;
    private Thread thread;
    private volatile long lastRefillAt;
    private final Timer timer;

    /**
     * A token filler adds tokens to a {@link ThreadDrivenTokenPool} at some rate.
     * By default, this rate is at least every millisecond +- scheduling jitter
     * in the JVM.
     *
     */
    public TokenFiller(RateSpec rateSpec, ThreadDrivenTokenPool tokenPool, NBNamedElement named, int hdrdigits) {
        this.rateSpec = rateSpec;
        this.tokenPool = tokenPool;
        this.timer = ActivityMetrics.timer(named, "tokenfiller", hdrdigits);
    }

    public TokenFiller apply(RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        return this;
    }

    private void stop() {
        this.running=false;
    }

    public TokenPool getTokenPool() {
        return tokenPool;
    }

    @Override
    public void run() {
        lastRefillAt = System.nanoTime();
        while (running) {
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
            tokenPool.refill(delta);
            timer.update(delta, TimeUnit.NANOSECONDS);
//            iteration++;

        }
    }

    public synchronized TokenFiller start() {
        this.tokenPool.refill(rateSpec.getNanosPerOp());

        thread = new Thread(this);
        thread.setName(this.toString());
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        thread.start();
        logger.debug("Starting token filler thread: " + this);
        return this;
    }

    @Override
    public String toString() {
        return "TokenFiller spec=" + rateSpec + " interval=" + this.interval + "ns pool:" + tokenPool +" running=" + running;
    }

//    public String getRefillLog() {
//        StringBuilder sb = new StringBuilder();
//        for (int iter = 0; iter < iteration; iter++) {
//            sb.append(times[iter]).append(" ").append(amounts[iter]).append("\n");
//        }
//        return sb.toString();
//    }

    public synchronized long restart() {
        this.lastRefillAt=System.nanoTime();
        logger.debug("Restarting token filler at " + lastRefillAt + " thread: " + this);
        long wait = this.tokenPool.restart();
        return wait;
    }

}

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
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TokenFiller implements Runnable {
    private static final Logger logger = LogManager.getLogger(TokenFiller.class);

    public static final double MIN_PER_SECOND = 10.0D;
    public static final double MAX_PER_SECOND = 1000.0D;
//    private final SysPerfData PERFDATA = SysPerf.get().getPerfData
//    (false);
    private final long interval = (long) 1.0E5;

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
    public TokenFiller(final RateSpec rateSpec, final ThreadDrivenTokenPool tokenPool, final NBLabeledElement labeled, final int hdrdigits) {
        this.rateSpec = rateSpec;
        this.tokenPool = tokenPool;
        timer = ActivityMetrics.timer(labeled, "tokenfiller", hdrdigits);
    }

    public TokenFiller apply(final RateSpec rateSpec) {
        this.rateSpec = rateSpec;
        return this;
    }

    private void stop() {
        running=false;
    }

    public TokenPool getTokenPool() {
        return this.tokenPool;
    }

    @Override
    public void run() {
        this.lastRefillAt = System.nanoTime();
        while (this.running) {
            final long nextRefillTime = this.lastRefillAt + this.interval;
            long thisRefillTime = System.nanoTime();
            while (thisRefillTime < nextRefillTime) {
//            while (thisRefillTime < lastRefillAt + interval) {
                final long parkfor = Math.max(nextRefillTime - thisRefillTime, 0L);
//                System.out.println(ANSI_Blue + "  parking for " + parkfor + "ns" + ANSI_Reset); System.out.flush();
                LockSupport.parkNanos(parkfor);
//                System.out.println(ANSI_Blue + "unparking for " + parkfor + "ns" + ANSI_Reset); System.out.flush();
                thisRefillTime = System.nanoTime();
            }

//            this.times[iteration]=thisRefillTime;
            final long delta = thisRefillTime - this.lastRefillAt;
//            this.amounts[iteration]=delta;
            this.lastRefillAt = thisRefillTime;

//            System.out.println(ANSI_Blue + this + ANSI_Reset); System.out.flush();
            this.tokenPool.refill(delta);
            this.timer.update(delta, TimeUnit.NANOSECONDS);
//            iteration++;

        }
    }

    public synchronized TokenFiller start() {
        tokenPool.refill(this.rateSpec.getNanosPerOp());

        this.thread = new Thread(this);
        this.thread.setName(toString());
        this.thread.setPriority(Thread.MAX_PRIORITY);
        this.thread.setDaemon(true);
        this.thread.start();
        TokenFiller.logger.debug("Starting token filler thread: {}", this);
        return this;
    }

    @Override
    public String toString() {
        return "TokenFiller spec=" + this.rateSpec + " interval=" + interval + "ns pool:" + this.tokenPool +" running=" + this.running;
    }

//    public String getRefillLog() {
//        StringBuilder sb = new StringBuilder();
//        for (int iter = 0; iter < iteration; iter++) {
//            sb.append(times[iter]).append(" ").append(amounts[iter]).append("\n");
//        }
//        return sb.toString();
//    }

    public synchronized long restart() {
        lastRefillAt=System.nanoTime();
        TokenFiller.logger.debug("Restarting token filler at {} thread: {}", this.lastRefillAt, this);
        final long wait = tokenPool.restart();
        return wait;
    }

}

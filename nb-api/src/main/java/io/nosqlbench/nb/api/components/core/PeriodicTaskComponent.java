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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PeriodicTaskComponent extends NBBaseComponent implements Runnable {

    private static final Logger logger = LogManager.getLogger(PeriodicTaskComponent.class);
    private final long intervalmillis;
    private final Lock lock = new ReentrantLock();
    private final Condition shutdownSignal = lock.newCondition();
    private final boolean oneLastTime;

    Thread thread;
    private boolean running = true;

    public PeriodicTaskComponent(
        NBComponent node,
        NBLabels extraLabels,
        long millis,
        boolean oneLastTime,
        String threadName
    ) {
        super(node, extraLabels);
        this.intervalmillis = millis;
        thread = Thread.ofVirtual().name(threadName).start(this);
        this.oneLastTime = oneLastTime;
    }

    protected abstract void task();

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long reportAt = now + intervalmillis;
        long waitfor = reportAt - now;

        while (running) {
            while (running && waitfor > 0) {
                boolean signalReceived = false;
                try {
                    lock.lock();
                    signalReceived = shutdownSignal.await(waitfor, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                } finally {
                    lock.unlock();
                }
                if (signalReceived) {
                    logger.debug("signal shutting down " + this);
                    return;
                }
                now = System.currentTimeMillis();
                waitfor = reportAt - now;
            }
//            logger.info("summarizing metrics to console");
            try {
                task();
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            } finally {
                reportAt = reportAt + (intervalmillis);
                now = System.currentTimeMillis();
                waitfor = reportAt - now;
            }
        }
        logger.info("shutting down periodic runnable component: " + description());
    }

    public void teardown() {
        logger.debug("shutting down " + this);

        lock.lock();
        running = false;
        shutdownSignal.signalAll();
        lock.unlock();
        logger.debug("signaled reporter thread to shut down " + description());

        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.warn("interrupted while joining thread");
        }

        if (oneLastTime) {
            logger.debug("running " + this + " one last time.");
            task();
        }

    }

}

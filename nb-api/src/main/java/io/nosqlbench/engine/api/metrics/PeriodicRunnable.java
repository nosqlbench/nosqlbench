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

package io.nosqlbench.engine.api.metrics;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This is a simple and light way to run a periodic task
 */
public class PeriodicRunnable<T extends Runnable> implements Runnable, AutoCloseable {
    private static final Logger logger = LogManager.getLogger(PeriodicRunnable.class);

    private final long intervalMillis;
    private final T action;
    private Thread thread;
    private volatile boolean running = false;

    public PeriodicRunnable(long intervalMillis, T action) {
        this.action = action;
        this.intervalMillis = intervalMillis;
    }

    public synchronized PeriodicRunnable<T> startDaemonThread() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName(action.toString());
        thread.start();
        return this;
    }

    @Override
    public synchronized void close()
    {
        running = false;
        try {
            thread.join(0L);
        } catch (InterruptedException ignored) {
        }
    }

    public synchronized PeriodicRunnable<T> startMainThread() {
        thread = new Thread(this);
        thread.setName(action.toString());
        thread.start();
        return this;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void run() {
        running = true;
        long nextEventTime = System.currentTimeMillis() + intervalMillis;
        while (running) {
            nextEventTime = awaitTime(intervalMillis, nextEventTime);
            logger.trace("invoking interval runnable " + action);
            action.run();
        }
    }

    private long awaitTime(long interval, long nextEventTime) {
        long duration = nextEventTime - System.currentTimeMillis();
        while (System.currentTimeMillis() < nextEventTime) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ignored) {
            }
        }
        return nextEventTime + interval;
    }

}

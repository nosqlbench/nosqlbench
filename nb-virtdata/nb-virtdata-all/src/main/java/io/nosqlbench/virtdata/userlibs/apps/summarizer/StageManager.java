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

package io.nosqlbench.virtdata.userlibs.apps.summarizer;

import io.nosqlbench.virtdata.userlibs.apps.valuechecker.IndexedThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntFunction;

public class StageManager implements Thread.UncaughtExceptionHandler, Runnable {
    private final static Logger logger = LogManager.getLogger(StageManager.class);

    private final ConcurrentLinkedDeque<Throwable> errors = new ConcurrentLinkedDeque<>();

    private final IndexedThreadFactory tf;
    private final ExecutorService pool;
    private final IntFunction<Runnable> tasks;
    private final int threads;

    Lock lock = new ReentrantLock();
    Condition goTime = lock.newCondition();
    private final ConcurrentLinkedQueue<Object> readyQueue = new ConcurrentLinkedQueue<>();

    public StageManager(int threads, IntFunction<Runnable> tasks) {
        this.threads = threads;
        this.tf = new IndexedThreadFactory("values-checker", this);
        this.pool = Executors.newFixedThreadPool(threads, tf);
        this.tasks = tasks;
    }

    @Override
    public void run() {
        for (int i = 0; i < threads; i++) {
            Runnable runnable = tasks.apply(i);
            RunBox box = new RunBox(runnable,this);
            pool.submit(box);
        }
        coordinateFor(threads,"tasks");
        coordinateFor(threads,"completion");
    }

    private final static class RunBox implements Runnable {
        private final Runnable inner;
        private final StageManager stage;

        public RunBox(Runnable inner, StageManager stage) {
            this.inner = inner;
            this.stage = stage;
        }

        @Override
        public void run() {
            logger.debug("blocking for start");
            stage.OnYourMarkGetSet(this);
            logger.debug("running");
            inner.run();
            logger.debug("blocking for completion");
            stage.OnYourMarkGetSet(this);
            logger.debug("returning");
        }
    }

    public void OnYourMarkGetSet(Object forWhat) {
        try {
            lock.lock();
            readyQueue.add(forWhat);
            logger.trace(() -> "awaiting signal for " + forWhat);
            goTime.await();
        } catch (Throwable e) {
            System.out.println("error while synchronizing: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void coordinateFor(int concurrency, String forWhat) {
        logger.trace(() -> "coordinating " + concurrency + " threads for " + forWhat);
        try {
            long delay = 1;
            long startedAt = System.currentTimeMillis();
            while (readyQueue.size() < concurrency) {
                long waitedFor = System.currentTimeMillis() -startedAt;
                if (waitedFor>10000L) {
                    throw new RuntimeException("Waited for " + waitedFor + " millis and not synchronized yet for " + forWhat);
                }
                logger.debug("threads ready for " + forWhat + ": " + readyQueue.size() + ", delaying " + delay + "ms");
                Thread.sleep(delay);
                delay = Math.min(1024, delay * 2);
                throwInjectedExceptions();
            }
            readyQueue.clear();
            lock.lock();
            goTime.signalAll();
        } catch (Exception e) {
            logger.error("Error while signaling threads:", e);
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

    }

    private synchronized void throwInjectedExceptions() {
        if (errors.peekFirst() != null) {
            int count = 0;
            for (Throwable error : errors) {
                System.out.print("EXCEPTION " + count++ + ": ");
                System.out.println(error.getMessage());
            }
            throw new RuntimeException(errors.peekFirst());
        }
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }

}

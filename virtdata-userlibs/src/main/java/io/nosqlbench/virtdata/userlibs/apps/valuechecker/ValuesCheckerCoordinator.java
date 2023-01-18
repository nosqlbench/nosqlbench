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

package io.nosqlbench.virtdata.userlibs.apps.valuechecker;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ValuesCheckerCoordinator implements Callable<RunData> {
    private static final Logger logger =
        LogManager.getLogger(ValuesCheckerCoordinator.class);

    private final String specifier;
    private final int threads;
    private final int bufsize;
    private final long end;
    private final long start;
    private final boolean printValues;
    private final ReentrantLock lock;
    private final Condition goTime;
    private final ConcurrentLinkedDeque<Throwable> errors = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedQueue<Integer> readyQueue = new ConcurrentLinkedQueue<>();

    ExecutorService pool;

    private long genTimeAccumulator = 0L;
    private long cmpTimeAccumulator = 0L;

    public ValuesCheckerCoordinator(
        String specifier,
        int threads,
        int bufsize,
        long start,
        long end,
        boolean printValues) {
        this.specifier = specifier;
        this.threads = threads;
        this.bufsize = bufsize;
        this.start = start;
        this.end = end;
        this.printValues = printValues;
        this.lock = new ReentrantLock();
        this.goTime = lock.newCondition();
    }


    public void run() {
        testConcurrentValues(threads, start, end, specifier);
        if (this.errors.size() > 0) {
            for (Throwable error : errors) {
                System.out.println(error.getMessage());
            }
            throw new RuntimeException("Errors in verification: " + this.errors);
        }
    }


    private void testConcurrentValues(
        int threads,
        long start,
        long end,
        String mapperSpec) {

        // Generate reference values in single-threaded mode.
        DataMapper<Object> mapper =
            VirtData.getOptionalMapper(specifier).orElseThrow(
                () -> new RuntimeException("Unable to map function for specifier: " + specifier)
            );

        final List<Object> reference = new CopyOnWriteArrayList<>();

        // Setup concurrent generator pool
        ValuesCheckerExceptionHandler valuesCheckerExceptionHandler =
            new ValuesCheckerExceptionHandler(this);
        IndexedThreadFactory tf =
            new IndexedThreadFactory("values-checker", valuesCheckerExceptionHandler);
        pool =
            Executors.newFixedThreadPool(threads, tf);

        logger.info("Checking [{}..{}) in chunks of {}", start, end, bufsize);


        for (int t = 0; t < threads; t++) {
            ValuesCheckerRunnable runnable;

            DataMapper<?> threadMapper = VirtData.getOptionalMapper(mapperSpec)
                .orElseThrow(
                    () -> new RuntimeException("Unable to map function for specifier: " + specifier)
                );
            runnable = new ValuesCheckerRunnable(
                start, end, bufsize, t, null, threadMapper,
                readyQueue, goTime, lock, reference, printValues
            );
            pool.execute(runnable);
        }

        logger.info("starting generation loops...");

        for (long intervalStart = 0; intervalStart < (end - start); intervalStart += bufsize) {

            String rangeInfo = "[" + intervalStart + ".." + (intervalStart + bufsize) + ")";

            long genStart = System.nanoTime();
            coordinateFor("generation start " + rangeInfo);
            throwInjectedExceptions();

            coordinateFor("generation complete " + rangeInfo);
            long genStop = System.nanoTime();
            long genTime = genStop - genStart;
            genTimeAccumulator += genTime;
            throwInjectedExceptions();

            System.out.print(".");
            System.out.flush();

            long cmpStart = System.nanoTime();
            coordinateFor("verification start " + rangeInfo);
            throwInjectedExceptions();

            coordinateFor("verification complete " + rangeInfo);
            long cmpEnd = System.nanoTime();
            long cmpTime = cmpEnd - cmpStart;
            cmpTimeAccumulator += cmpTime;
            throwInjectedExceptions();

            System.out.print(".");
            System.out.flush();

        }
        System.out.println("\n");
        pool.shutdown();
        try {
            pool.awaitTermination(60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    synchronized void handleException(Thread t, Throwable e) {
        this.errors.add(e);
        if (pool != null) {
            pool.shutdownNow();
        }

    }

    private void coordinateFor(String forWhat) {
        logger.trace(() -> "coordinating " + threads + " threads for " + forWhat);
        try {
            long delay = 1;
            while (readyQueue.size() < threads) {
//                logger.debug(() -> "threads ready for " + forWhat + ": " + readyQueue.size() + ", delaying " + delay + "ms");
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

    @Override
    public RunData call() throws Exception {
        run();
        return new RunData(
            this.specifier,
            this.threads,
            this.start,
            this.end,
            this.bufsize,
            this.printValues,
            ((double) genTimeAccumulator / 1000000.0D),
            ((double) cmpTimeAccumulator / 1000000.0D)
        );
    }
}

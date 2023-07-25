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

import io.nosqlbench.engine.api.util.Colors;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.api.testutils.Bounds;
import io.nosqlbench.api.testutils.Perf;
import io.nosqlbench.api.testutils.Result;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RateLimiterPerfTestMethods {

//    public Perf testFindOverheads(RateLimiter rl) {
//        List<Result> results = new ArrayList<>();
//        Perf perf = new Perf("perf tests for " + rl);
//        perf.add(rateLimiterSingleThreadedConvergence(rl));
////        perf.add(systemTimeOverhead(rl));
////        perf.add(conditionOverhead());
//        return perf;
//    }

    public Result systemTimeOverhead(final RateLimiter rl) {
        final Bounds bounds = new Bounds(1000, 2);
        final Perf perf = new Perf("nanotime");

        while (!perf.isConverged(Result::getOpsPerSec, 0.01d, 3)) {
            System.out.println("testing with opcount=" + bounds.getNextValue());
            final long start = System.nanoTime();
            for (long iter = 0; iter < bounds.getValue(); iter++) {
                final long result = System.nanoTime();
            }
            final long end = System.nanoTime();

            perf.add("nanotime/" + bounds.getValue(), start, end, bounds.getValue());
        }

        final double[] deltas = perf.getDeltas(Result::getOpsPerSec);
        return perf.getLastResult();
    }

    public Result rateLimiterSingleThreadedConvergence(final Function<RateSpec, RateLimiter> rlf, final RateSpec rs, final long startingCycles, final double margin) {
        //rl.applyRateSpec(rl.getRateSpec().withOpsPerSecond(1E9));
        final Bounds bounds = new Bounds(startingCycles, 2);
        final Perf perf = new Perf("nanotime");

        while (!perf.isConverged(Result::getOpsPerSec, margin, 3)) {
            System.out.println("testing with opcount=" + bounds.getNextValue() + " spec=" + rs);

            final RateLimiter rl = rlf.apply(rs);
            final long start = System.nanoTime();
            for (long iter = 0; iter < bounds.getValue(); iter++) {
                final long result = rl.maybeWaitForOp();
            }
            final long end = System.nanoTime();

            perf.add("rl/" + bounds.getValue(), start, end, bounds.getValue());
            System.out.println(perf.getLastResult());
        }

        return perf.getLastResult();
    }


    /**
     * This test method will call {@link  RateLimiter#maybeWaitForOp()} on a rate limiter with a sequence of different
     * getOpsPerSec specifiers. For each 4-tuple in the second varargs argument, the following fields
     * are used to control how the getOpsPerSec limiter is configured and called:
     *
     * <OL>
     * <LI>count - how many times to call maybeWaitForOp</LI>
     * <LI>getOpsPerSec - the getOpsPerSec to set the getOpsPerSec limiter to</LI>
     * <LI>divisions - the number of sub-segments to iterate and record</LI>
     * <LI>clientrate - the artificially limited client getOpsPerSec</LI>
     * </OL>
     *
     * @param count_rate_division_clientrate
     * @return
     */
    long[] testRateChanges(final RateLimiter rl, final int... count_rate_division_clientrate) {
        System.out.println("Running " + Thread.currentThread().getStackTrace()[1].getMethodName());

        final List<Long> results = new ArrayList<>();

        for (int idx = 0; idx < count_rate_division_clientrate.length; idx += 4) {
            final int count = count_rate_division_clientrate[idx];
            final int rate = count_rate_division_clientrate[idx + 1];
            final int divisions = count_rate_division_clientrate[idx + 2];
            final int clientrate = count_rate_division_clientrate[idx + 3];
            final long clientnanos = (long) (1_000_000_000.0D / clientrate);

            if (rl instanceof DiagUpdateRate) {
                ((DiagUpdateRate) rl).setDiagModulo(count / divisions);
                System.out.println("updating every " + count / divisions + " calls (" + count + '/' + divisions + ')');
            }
            System.out.println("count=" + count + ", getOpsPerSec=" + rate + ", div=" + divisions + ", clientrate=" + clientrate);
            System.out.println("client nanos: " + clientnanos);

            final long startAt = System.nanoTime();
            rl.applyRateSpec(rl.getRateSpec().withOpsPerSecond(rate));
            final int perDivision = count / divisions;
            long divDelay = 0L;
            for (int div = 0; div < divisions; div++) {
                long then = System.nanoTime();
                for (int i = 0; i < perDivision; i++) {
                    then += clientnanos;
                    rl.maybeWaitForOp();
                    while (System.nanoTime() < then) {
                    }
                }
                divDelay = rl.maybeWaitForOp();
                results.add(divDelay);
            }

            final long endAt = System.nanoTime();
            final double duration = (endAt - startAt) / 1000000000.0d;
            final double acqops = count / duration;

            System.out.println(rl);

            System.out.println(Colors.ANSI_Blue +
                    String.format(
                            "spec: %s\n count: %9d, duration %.5fS, acquires/s %.3f, nanos/op: %f\n delay: %d (%.5fS)",
                            rl.getRateSpec(),
                            count, duration, acqops, 1_000_000_000.0d / acqops, divDelay, divDelay / 1_000_000_000.0d) +
                    Colors.ANSI_Reset);

        }

        final long[] delays = results.stream().mapToLong(Long::longValue).toArray();

        final String delaySummary = Arrays.stream(delays).mapToDouble(d -> d / 1_000_000_000.0D).mapToObj(d -> String.format("%.3f", d))
                .collect(Collectors.joining(","));
        System.out.println("delays in seconds:\n" + delaySummary);
        System.out.println("delays in ns:\n" + Arrays.toString(delays));

        return delays;

    }

    public Result rateLimiterContendedConvergence(final int threads, final Function<RateSpec, RateLimiter> rlFunc, final RateSpec rateSpec, final int initialIterations, final double margin) {
        final Bounds bounds = new Bounds(initialIterations, 2);
        final Perf perf = new Perf("contended with " + threads + " threads");

        while (!perf.isConverged(Result::getOpsPerSec, margin, 3)) {
            final Perf delegateperf = this.testRateLimiterMultiThreadedContention(rlFunc, rateSpec, initialIterations, threads);
            perf.add(delegateperf.getLastResult());
        }
        return perf.getLastResult();
    }

    /**
     * This a low-overhead test for multi-threaded access to the same getOpsPerSec limiter. It calculates the
     * effective concurrent getOpsPerSec under atomic contention.
     */
    public Perf testRateLimiterMultiThreadedContention(final Function<RateSpec, RateLimiter> rlFunc, final RateSpec spec, final long iterations, final int threadCount) {
        System.out.println("Running " + Thread.currentThread().getStackTrace()[1].getMethodName());

        final RateLimiter rl = rlFunc.apply(spec);
        final double rate = spec.getRate();
        final int iterationsPerThread = (int) (iterations / threadCount);
        if (Integer.MAX_VALUE <= iterationsPerThread)
            throw new RuntimeException("iterations per thread too high with (count,threads)=(" + iterations + ',' + threadCount);
        final TestExceptionHandler errorhandler = new TestExceptionHandler();
        final TestThreadFactory threadFactory = new TestThreadFactory(errorhandler);
        final ExecutorService tp = Executors.newFixedThreadPool(threadCount + 1, threadFactory);

        System.out.format("Running %,d iterations split over %,d threads (%,d per) at %,.3f ops/s\n", iterations, threadCount, iterations / threadCount, rate);
        final Acquirer[] threads = new Acquirer[threadCount];
        final DeltaHdrHistogramReservoir stats = new DeltaHdrHistogramReservoir(NBLabels.forKV("name", "times"), 5);

        final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);

        final RateLimiterStarter starter = new RateLimiterStarter(barrier, rl);

        //            threads[i] = new RateLimiterPerfTestMethods.Acquirer(i, rl, (int) (iterations / threadCount), stats, barrier);
        for (int i = 0; i < threadCount; i++) threads[i] = new Acquirer(i, rl, iterationsPerThread, stats, barrier);

        tp.execute(starter);

        System.out.println(rl);
        System.out.format("submitting (%d threads)...\n", threads.length);
        final List<Future<Result>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) futures.add(tp.submit((Callable<Result>) threads[i]));
        System.out.format("submitted (%d threads)...\n", threads.length);

        try {
            tp.shutdown();
            if (!tp.awaitTermination(1000, TimeUnit.SECONDS))
                throw new RuntimeException("Failed to shutdown thread pool.");
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        errorhandler.throwIfAny();

        System.out.println(rl);

        final Perf aggregatePerf = new Perf("contended with " + threadCount + " threads for " + iterations + " iterations for " + rl.getRateSpec().toString());
        futures.stream().map(f -> {
            try {
                return f.get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }).forEachOrdered(aggregatePerf::add);

//        System.out.println(aggregatePerf);

//        if (rl instanceof HybridRateLimiter) {
//            String refillLog = ((HybridRateLimiter) rl).getRefillLog();
//            System.out.println("refill log:\n" + refillLog);
//        }
        final Perf perf = aggregatePerf.reduceConcurrent();
        return perf;

    }

    private static class RateLimiterStarter implements Runnable {
        private final CyclicBarrier barrier;
        private final RateLimiter rl;

        public RateLimiterStarter(final CyclicBarrier barrier, final RateLimiter rl) {
            this.barrier = barrier;
            this.rl = rl;
        }

        @Override
        public void run() {
            try {
//                System.out.println("awaiting barrier (starter) (" + barrier.getNumberWaiting() + " awaiting)");
                this.barrier.await(60, TimeUnit.SECONDS);
//                System.out.println("started the rate limiter (starter) (" + barrier.getNumberWaiting() + " awaiting)");

            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            this.rl.start();
        }
    }

    private static class TestExceptionHandler implements UncaughtExceptionHandler {
        public List<Throwable> throwables = new ArrayList<>();
        public List<Thread> threads = new ArrayList<>();

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            this.threads.add(t);
            this.throwables.add(e);
            System.out.println("uncaught exception on thread " + t.getName() + ": " + e.toString());
        }

        public void throwIfAny() {
            if (0 < throwables.size()) throw new RuntimeException(this.throwables.get(0));
        }
    }

    private static class Acquirer implements Callable<Result>, Runnable {
        private final RateLimiter limiter;
        private final int threadIdx;
        private final DeltaHdrHistogramReservoir reservoir;
        private final CyclicBarrier barrier;
        private final long iterations;

        public Acquirer(final int i, final RateLimiter limiter, final int iterations, final DeltaHdrHistogramReservoir reservoir, final CyclicBarrier barrier) {
            threadIdx = i;
            this.limiter = limiter;
            this.iterations = iterations;
            this.reservoir = reservoir;
            this.barrier = barrier;
        }

        @Override
        public Result call() {
//            synchronized (barrier) {
            try {
                if (0 == this.threadIdx) System.out.println("awaiting barrier");
                this.barrier.await(60, TimeUnit.SECONDS);
                if (0 == this.threadIdx) System.out.println("starting all threads");

            } catch (final Exception be) {
                throw new RuntimeException(be); // This should not happen unless the test is broken
            }
//            }
            final long startTime = System.nanoTime();
            for (int i = 0; i < this.iterations; i++) {
                final long time = this.limiter.maybeWaitForOp();
            }
            final long endTime = System.nanoTime();
            return new Result("thread " + threadIdx, startTime, endTime, this.iterations);
        }

        @Override
        public void run() {
            for (int i = 0; i < this.iterations; i++) this.limiter.maybeWaitForOp();
        }
    }


    private static class TestThreadFactory implements ThreadFactory {

        private final UncaughtExceptionHandler handler;

        public TestThreadFactory(final UncaughtExceptionHandler uceh) {
            handler = uceh;
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(this.handler);
            return t;
        }
    }


}

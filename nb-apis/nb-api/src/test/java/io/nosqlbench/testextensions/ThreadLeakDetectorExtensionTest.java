/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.testextensions;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases demonstrating the ThreadLeakDetectorExtension functionality.
 * This class shows both positive cases (proper cleanup) and negative cases (thread leaks).
 */
@ExtendWith(ThreadLeakDetectorExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("unit")
public class ThreadLeakDetectorExtensionTest {

    /**
     * Test case that properly cleans up threads - should pass
     */
    @Test
    @Order(1)
    public void testProperThreadCleanup() throws InterruptedException {
        // Create an executor service
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Submit some tasks
        CountDownLatch latch = new CountDownLatch(2);
        executor.submit(() -> {
            try {
                Thread.sleep(50);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(50);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Wait for tasks to complete
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Tasks should complete");

        // Properly shutdown the executor
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS),
            "Executor should terminate");

        // This test should pass as all threads are properly cleaned up
    }

    /**
     * Test case with daemon threads - should pass as daemon threads are ignored
     */
    @Test
    @Order(2)
    public void testDaemonThreadsIgnored() {
        // Create a daemon thread
        Thread daemonThread = new Thread(() -> {
            try {
                // Daemon thread that runs briefly
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TestDaemonThread");

        daemonThread.setDaemon(true);
        daemonThread.start();

        // Test passes even though daemon thread might still be running
        // because ThreadLeakDetectorExtension ignores daemon threads by default
    }

    /**
     * Test case that creates a thread but properly cleans it up
     */
    @Test
    @Order(3)
    public void testManualThreadCleanup() throws InterruptedException {
        AtomicBoolean shouldRun = new AtomicBoolean(true);

        Thread worker = new Thread(() -> {
            while (shouldRun.get()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ManualWorkerThread");

        worker.start();

        // Do some work
        Thread.sleep(50);

        // Stop the thread properly
        shouldRun.set(false);
        worker.interrupt();
        worker.join(1000);

        assertFalse(worker.isAlive(), "Worker thread should be terminated");
    }

    /**
     * Test case that would normally leak a thread but we'll disable the extension for this one
     * This demonstrates what would happen without proper cleanup
     */
    @Test
    @Order(4)
    @Disabled("This test intentionally leaks a thread - enable to see failure")
    public void testIntentionalThreadLeak() {
        // Create a thread that will outlive the test
        Thread leakyThread = new Thread(() -> {
            try {
                // This thread will run for a long time
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "LeakyThread");

        leakyThread.start();

        // Test ends without cleaning up the thread
        // This would be detected by ThreadLeakDetectorExtension and fail
    }

    /**
     * Test case with thread pool that is not properly shutdown
     */
    @Test
    @Order(5)
    @Disabled("This test intentionally leaks threads - enable to see failure")
    public void testExecutorServiceLeak() {
        // Create an executor but don't shut it down
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit a task
        executor.submit(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Forgetting to call executor.shutdown()
        // This would be detected as a thread leak
    }

    /**
     * Test case showing proper use of cached thread pool
     */
    @Test
    @Order(6)
    public void testCachedThreadPoolCleanup() throws InterruptedException {
        ExecutorService cachedPool = Executors.newCachedThreadPool();

        // Submit multiple short tasks
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            cachedPool.submit(() -> {
                try {
                    Thread.sleep(20);
                    System.out.println("Task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Properly shutdown and wait
        cachedPool.shutdown();
        assertTrue(cachedPool.awaitTermination(2, TimeUnit.SECONDS),
            "Cached pool should terminate");
    }

    /**
     * Test that shows threads with special names are ignored
     */
    @Test
    @Order(7)
    public void testIgnoredThreadPatterns() {
        // Create a thread with a name that matches the ignore pattern
        Thread ignoredThread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ForkJoinPool-1-worker-1"); // This name matches the ignore pattern

        ignoredThread.start();

        // Test passes even though thread might still be running
        // because the name matches the ignore pattern in ThreadLeakDetectorExtension
    }

    /**
     * Nested test class to demonstrate extension works across test classes
     */
    @Nested
    @DisplayName("Nested Thread Tests")
    class NestedThreadTests {

        @Test
        public void testNestedThreadCleanup() throws InterruptedException {
            Thread nestedWorker = new Thread(() -> {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "NestedWorker");

            nestedWorker.start();
            nestedWorker.join(1000);

            assertFalse(nestedWorker.isAlive(), "Nested worker should be terminated");
        }
    }
}
package io.nosqlbench.adapters.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.LongFunction;

/**
 * <P>This cache implementation packs referents into an atomic array, keeping things as compact as possible,
 * allowing auto-resizing, size tracking, and supporting concurrent access with minimal locking. It also uses a bitset
 * to track the referent indices for enumeration or traversal.</P>
 *
 * <P>In order to protect against unexpected OOM scenarios, the maximum index is defaulted to 1000000. If you want
 * to have index caches bigger than this, pass ina higher limit.</P>
 *
 * @param <T>
 */
public class ConcurrentIndexCache<T> implements Iterable<T>, AutoCloseable {
    private final static Logger logger = LogManager.getLogger("SPACECACHE");
    private final AtomicReference<AtomicReferenceArray<Object>> cacheRef;
    private static final int GROWTH_FACTOR = 2;
    private final LongFunction<T> valueLoader;
    private final BitSet active = new BitSet();
    private final String label;
    private volatile int count = 0;
    private int maxIndex = 1000000;

    // Constructor with initial capacity
    public ConcurrentIndexCache(String label, LongFunction<T> valueLoader, int maxIndex) {
        this.label = label;
        this.valueLoader = valueLoader;
        this.cacheRef = new AtomicReference<>(new AtomicReferenceArray<>(1));
        this.maxIndex = maxIndex;
    }

    public ConcurrentIndexCache(String label, LongFunction<T> valueLoader) {
        this(label, valueLoader, 1000000);
    }

    public ConcurrentIndexCache(String label) {
        this(label, null);
    }

    public T get(long key) {
        if (valueLoader == null) {
            throw new IllegalStateException("Cache created without a default value loader cannot use get(long)");
        }
        return get(key, valueLoader);
    }

    @SuppressWarnings("unchecked")
    public T get(long longkey, LongFunction<T> defaultValueLoader) {
        if (longkey > maxIndex || longkey < 0) {
            throw new BasicError("index " + longkey + " too high (outside 0.." + maxIndex + ")");
        }
        int key = (int) longkey;

        AtomicReferenceArray<Object> currentCache = cacheRef.get();

        if (key >= currentCache.length()) {
            resize(key);
            currentCache = cacheRef.get(); // Get the updated array after resizing
        }

        Object value = currentCache.get(key);

        // Fast path - already computed value
        if (value != null) {
            if (value instanceof FutureTask) {
                // Wait for computation to complete
                try {
                    logger.trace(() -> "waiting for future for index[ " + key + " ] for [ " + label + " ] cache");
                    return ((FutureTask<T>) value).get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BasicError("Interrupted waiting for cache computation", e);
                } catch (ExecutionException e) {
                    // Computation failed, clear the future and re-throw
                    currentCache.compareAndSet(key, value, null);
                    throw new BasicError("Cache computation failed", e.getCause());
                } catch (CancellationException e) {
                    // Future was cancelled
                    currentCache.compareAndSet(key, value, null);
                    // Fall through to re-attempt computation
                }
            } else {
                // Direct value - return it
                logger.trace(() -> "returning    index[ " + key + " ] for [ " + label + " ] cache");
                return (T) value;
            }
        }

        if (defaultValueLoader == null) {
            return null; // No loader provided
        }

        // Create a FutureTask for the computation
        FutureTask<T> futureTask = new FutureTask<>(() -> defaultValueLoader.apply(key));

        // Try to set the future as a placeholder
        if (currentCache.compareAndSet(key, null, futureTask)) {
            // This thread won the race to initialize this slot
            logger.debug(() -> "initializing index[ " + key + " ] for [ " + label + " ] cache");

            try {
                // Execute the computation
                futureTask.run();
                // Get the result (won't block since we just ran it)
                T result = futureTask.get();

                // Replace the future with the actual result
                currentCache.compareAndSet(key, futureTask, result);

                // Update metadata
                synchronized (active) {
                    if (!active.get(key)) {
                        active.set(key);
                        count++;
                    }
                }

                return result;
            } catch (Exception e) {
                // If computation fails, clear the slot
                currentCache.compareAndSet(key, futureTask, null);
                if (e instanceof ExecutionException) {
                    throw new BasicError("Error computing cache value", e.getCause());
                }
                throw new BasicError("Error computing cache value", e);
            }
        } else {
            // Another thread beat us - retry from the beginning
            return get(longkey, defaultValueLoader);
        }
    }

    // Method to resize the array if key exceeds current capacity
    private synchronized void resize(int key) {
        if (key > maxIndex) {
            throw new BasicError("index " + key + " too high (outside 0.." + maxIndex + ")");
        }
        AtomicReferenceArray<Object> currentCache = cacheRef.get();
        if (key < currentCache.length()) {
            return; // Double-check locking to avoid multiple resizes
        }

        // Calculate new size (at least as large as key + 1)
        int newCapacity = Math.max(currentCache.length() * GROWTH_FACTOR, key + 1);
        AtomicReferenceArray<Object> newCache = new AtomicReferenceArray<>(newCapacity);

        // Copy elements from old cache to new cache
        for (int i = 0; i < currentCache.length(); i++) {
            newCache.set(i, currentCache.get(i));
        }

        // Atomically update the cache reference
        cacheRef.set(newCache);
        logger.debug(() -> "resized cache from " + currentCache.length() + " to " + newCapacity + " for [ " + label + " ]");
    }

    // Optional: Method to remove an entry
    @SuppressWarnings("unchecked")
    public T remove(int key) {
        AtomicReferenceArray<Object> currentCache = cacheRef.get();
        if (key >= currentCache.length()) {
            return null; // Key is out of bounds
        }

        Object oldValue = currentCache.getAndSet(key, null);
        if (oldValue == null) {
            return null;
        }

        synchronized (active) {
            if (active.get(key)) {
                active.clear(key);
                count--;
            }
        }

        if (oldValue instanceof FutureTask) {
            // Cancel the future if it's still running
            ((FutureTask<?>) oldValue).cancel(false);
            return null;
        }

        T result = (T) oldValue;
        if (result instanceof AutoCloseable ac) {
            try {
                ac.close();
            } catch (Exception e) {
                logger.error("Error closing AutoCloseable value for key " + key, e);
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    // Optional: Method to clear the entire cache
    @SuppressWarnings("unchecked")
    public void clear() {
        AtomicReferenceArray<Object> oldCache = cacheRef.getAndSet(new AtomicReferenceArray<>(1));

        synchronized (active) {
            active.clear();
            count = 0;
        }

        // Close any AutoCloseable values and cancel futures
        for (int i = 0; i < oldCache.length(); i++) {
            Object value = oldCache.get(i);
            if (value instanceof FutureTask) {
                ((FutureTask<?>) value).cancel(false);
            } else if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Exception e) {
                    logger.error("Error closing AutoCloseable value during clear", e);
                }
            }
        }
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ElementIterator<>(this);
    }

    public int size() {
        return this.count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void close() throws Exception {
        Exception firstException = null;

        synchronized (active) {
            // Iterate over all active indices
            PrimitiveIterator.OfInt indexIterator = active.stream().iterator();
            AtomicReferenceArray<Object> currentCache = cacheRef.get();

            while (indexIterator.hasNext()) {
                int index = indexIterator.nextInt();
                Object entry = currentCache.get(index);

                // Handle different types of entries
                if (entry instanceof FutureTask) {
                    FutureTask<T> task = (FutureTask<T>) entry;
                    if (task.isDone() && !task.isCancelled()) {
                        try {
                            // Get the completed value
                            T value = task.get();
                            if (value instanceof AutoCloseable) {
                                ((AutoCloseable) value).close();
                            }
                        } catch (Exception e) {
                            logger.error("Error processing future during close for index " + index, e);
                            if (firstException == null) firstException = e;
                        }
                    } else {
                        task.cancel(false);
                    }
                } else if (entry instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) entry).close();
                    } catch (Exception e) {
                        logger.error("Error closing AutoCloseable value for index " + index, e);
                        if (firstException == null) firstException = e;
                    }
                }
            }
        }

        if (firstException != null) {
            throw firstException;
        }
    }

    public static final class ElementIterator<T> implements @NotNull Iterator<T> {
        private final PrimitiveIterator.OfInt indexIterator;
        private final ConcurrentIndexCache<T> indexCache;
        private T nextValue = null;
        private boolean hasNext = false;

        public ElementIterator(ConcurrentIndexCache<T> ts) {
            this.indexCache = ts;
            synchronized (ts.active) {
                this.indexIterator = ts.active.stream().iterator();
            }
            advance();
        }

        @SuppressWarnings("unchecked")
        private void advance() {
            hasNext = false;
            AtomicReferenceArray<Object> currentCache = indexCache.cacheRef.get();

            while (indexIterator.hasNext() && !hasNext) {
                int index = indexIterator.nextInt();
                if (index >= currentCache.length()) continue;

                Object entry = currentCache.get(index);
                if (entry instanceof FutureTask) {
                    FutureTask<T> task = (FutureTask<T>) entry;
                    if (task.isDone() && !task.isCancelled()) {
                        try {
                            nextValue = task.get();
                            hasNext = nextValue != null;
                        } catch (Exception e) {
                            // Skip this entry
                        }
                    }
                } else if (entry != null) {
                    nextValue = (T) entry;
                    hasNext = true;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            T result = nextValue;
            advance();
            return result;
        }
    }
}
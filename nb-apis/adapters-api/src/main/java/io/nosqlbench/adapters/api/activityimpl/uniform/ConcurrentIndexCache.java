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
    private final AtomicReference<AtomicReferenceArray<T>> cacheRef;
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
        return get(key, valueLoader);
    }

    public T get(long longkey, LongFunction<T> defaultValueLoader) {

        if (longkey > maxIndex) {
            if (longkey > maxIndex) {
                throw new BasicError("index " + longkey + " too high (outside 0.." + maxIndex + ")");
            }
        }
        int key = (int) longkey;

        AtomicReferenceArray<T> currentCache = cacheRef.get();

        if (key >= currentCache.length()) {
            resize(key);
            currentCache = cacheRef.get(); // Get the updated array after resizing
        }

        T value = currentCache.get(key);
        if (value == null) {
            T newValue;
            synchronized (defaultValueLoader) { // limit construction concurrency to 1 for now to avoid wasteful races
                newValue = defaultValueLoader.apply(key);
            }
            // Atomically set the value if it's still null (compare-and-set)
            if (currentCache.compareAndSet(key, null, newValue)) {
                active.set(key);
                count++;
                logger.debug(() -> "initializing index[ " + key + " ] for [ " + label + " ] cache");
                return newValue;
            } else {
                // Another thread might have set the value, so return the existing one
                return currentCache.get(key);
            }
        }
        logger.trace(() -> "returning    index[ " + key + " ] for [ " + label + " ] cache");

        return value;
    }

    // Method to resize the array if key exceeds current capacity
    private synchronized void resize(int key) {
        if (key > maxIndex) {
            throw new BasicError("index " + key + " too high (outside 0.." + maxIndex + ")");
        }
        AtomicReferenceArray<T> currentCache = cacheRef.get();
        if (key < currentCache.length()) {
            return; // Double-check locking to avoid multiple resizes
        }

        // Calculate new size (at least as large as key + 1)
        int newCapacity = Math.max(currentCache.length() * GROWTH_FACTOR, key + 1);
        AtomicReferenceArray<T> newCache = new AtomicReferenceArray<>(newCapacity);

        // Copy elements from old cache to new cache
        for (int i = 0; i < currentCache.length(); i++) {
            newCache.set(i, currentCache.get(i));
        }

        // Atomically update the cache reference
        cacheRef.set(newCache);
    }

    // Optional: Method to remove an entry
    public T remove(int key) {
        AtomicReferenceArray<T> currentCache = cacheRef.get();
        if (key >= currentCache.length()) {
            return null; // Key is out of bounds
        }

        T oldValue = currentCache.get(key);
        if (oldValue instanceof AutoCloseable ac ) {
            try {
                ac.close();
            } catch (Exception e) {
                logger.error("Error closing AutoCloseable value for key " + key,e);
                throw new RuntimeException(e);
            }
        }
        currentCache.set(key, null);  // Set the slot to null (safe for garbage collection)
        active.clear(key);
        count--;
        return oldValue;
    }

    // Optional: Method to clear the entire cache
    public void clear() {
        cacheRef.set(new AtomicReferenceArray<>(1));
        active.clear();
        count = 0;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ElementIterator<>(this);
    }

    public int size() {
        return this.count;
    }

    @Override
    public void close() throws Exception {
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (element instanceof AutoCloseable ac) {
                try {
                    ac.close();
                } catch (Exception e) {
                    logger.error("Error closing AutoCloseable value for key " + element,e);
//                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static final class ElementIterator<T> implements @NotNull Iterator<T> {

        private final PrimitiveIterator.OfInt iterator;
        private final ConcurrentIndexCache<T> indexCache;

        public ElementIterator(ConcurrentIndexCache<T> ts) {
            this.indexCache = ts;
            iterator = ts.active.stream().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            int index = this.iterator.nextInt();
            return indexCache.get(index);
        }
    }
}

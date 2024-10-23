package io.nosqlbench.adapters.api.activityimpl.uniform;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.nb.api.errors.OpConfigError;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.LongFunction;

public class ConcurrentIndexCache<T> implements Iterable<T> {
    private final AtomicReference<AtomicReferenceArray<T>> cacheRef;
    private static final int GROWTH_FACTOR = 2;
    private final LongFunction<T> valueLoader;
    private final BitSet active = new BitSet();

    // Constructor with initial capacity
    public ConcurrentIndexCache(LongFunction<T> valueLoader) {
        this.valueLoader = valueLoader;
        this.cacheRef = new AtomicReference<>(new AtomicReferenceArray<>(1));
    }

    // Get or compute value if absent, using a valueLoader function
    public T get(long longkey) {
        if (longkey > Integer.MAX_VALUE) {
            throw new OpConfigError("space index must be between 0 and " + (Integer.MAX_VALUE - 1) + " inclusive");
        }
        int key = (int)longkey;

        AtomicReferenceArray<T> currentCache = cacheRef.get();

        if (key >= currentCache.length()) {
            resize(key);
            currentCache = cacheRef.get(); // Get the updated array after resizing
        }

        T value = currentCache.get(key);
        if (value == null) {
            T newValue;
            synchronized (valueLoader) { // limit construction concurrency to 1 for now to avoid wasteful races
                newValue = valueLoader.apply(key);
            }
            // Atomically set the value if it's still null (compare-and-set)
            if (currentCache.compareAndSet(key, null, newValue)) {
                active.set(key);
                return newValue;
            } else {
                // Another thread might have set the value, so return the existing one
                return currentCache.get(key);
            }
        }
        return value;
    }

    // Method to resize the array if key exceeds current capacity
    private synchronized void resize(int key) {
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
        currentCache.set(key, null);  // Set the slot to null (safe for garbage collection)
        active.clear(key);
        return oldValue;
    }

    // Optional: Method to clear the entire cache
    public void clear() {
        cacheRef.set(new AtomicReferenceArray<>(1));
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ElementIterator<>(this);
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

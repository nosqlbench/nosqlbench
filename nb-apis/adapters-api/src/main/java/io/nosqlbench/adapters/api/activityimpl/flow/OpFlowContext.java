/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapters.api.activityimpl.flow;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/// Mutable context keyed by flow id and optionally space name. Designed for reuse per stride slot
/// and cleared at the start of each flow invocation. Implemented without thread-local storage to
/// remain virtual-thread-friendly, and without object pooling beyond simple clear-and-reuse.
///
/// ```text
/// flow-id (stride slot)
///   ├─ space#0 (default) : Object[] by ordinal
///   ├─ space#1           : Object[] by ordinal
///   └─ space#2           : Object[] by ordinal
/// ```
///
/// Typical lifecycle:
/// 1. {@link #reset(long)} at flow start to clear prior values
/// 2. capture/write via ordinal setters (optionally space-qualified)
/// 3. inject/read via ordinal getters (optionally space-qualified)
///
/// Reset cost is limited to clearing existing arrays; no expensive reinitialization occurs.
public final class OpFlowContext {

    private final long flowId;
    private final int initialCapacity;
    private final int initialSpaceCapacity;
    private volatile Object[][] spaceSlots;
    private final ConcurrentHashMap<String, Integer> spaceOrdinals = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> valueOrdinals = new ConcurrentHashMap<>();
    private final AtomicInteger nextOrdinal = new AtomicInteger();
    private volatile long lastCycle;

    public OpFlowContext(long flowId, int initialCapacity, int initialSpaceCapacity) {
        this.flowId = flowId;
        this.initialCapacity = Math.max(1, initialCapacity);
        this.initialSpaceCapacity = Math.max(1, initialSpaceCapacity);
        this.spaceSlots = new Object[this.initialSpaceCapacity][];
        this.spaceSlots[0] = new Object[this.initialCapacity]; // default space at index 0
        this.spaceOrdinals.put("", 0);
    }

    public long flowId() {
        return flowId;
    }

    public long lastCycle() {
        return lastCycle;
    }

    /// Get a value by ordinal within the default space.
    public Object get(int ordinal) {
        return get(ordinal, 0);
    }

    /// Get a value by ordinal within a numbered space (0 = default).
    public Object get(int ordinal, int spaceIndex) {
        Object[] slot = spaceArray(spaceIndex);
        return ordinal < slot.length ? slot[ordinal] : null;
    }

    /// Set a value by ordinal within the default space.
    public void set(int ordinal, Object value) {
        set(ordinal, value, 0);
    }

    /// Set a value by ordinal within a numbered space (0 = default).
    public void set(int ordinal, Object value, int spaceIndex) {
        Object[] slot = spaceArray(spaceIndex);
        if (ordinal >= slot.length) {
            slot = grow(slot, ordinal + 1, spaceIndex);
        }
        slot[ordinal] = value;
    }

    /// Resolve a space name to an index (assigns a new index if not present) and set value.
    public void set(int ordinal, Object value, String spaceName) {
        int idx = resolveSpaceOrdinal(spaceName);
        set(ordinal, value, idx);
    }

    /// Resolve a value name to an ordinal and set it within a numbered space.
    public void set(String name, Object value, int spaceIndex) {
        int ordinal = resolveValueOrdinal(name);
        set(ordinal, value, spaceIndex);
    }

    /// Resolve a value name to an ordinal and get it within a numbered space.
    public Object get(String name, int spaceIndex) {
        Integer ordinal = valueOrdinals.get(name);
        if (ordinal == null) {
            return null;
        }
        return get(ordinal, spaceIndex);
    }

    /// Resolve a space name to an index (assigns a new index if not present) and get value.
    public Object get(int ordinal, String spaceName) {
        int idx = resolveSpaceOrdinal(spaceName);
        return get(ordinal, idx);
    }

    /**
     * Clear all stored values and record the cycle that initiated the reset.
     */
    public void reset(long cycle) {
        lastCycle = cycle;
        for (int i = 0; i < spaceSlots.length; i++) {
            Object[] slot = spaceSlots[i];
            if (slot != null) {
                Arrays.fill(slot, null);
            }
        }
    }

    private Object[] spaceArray(int spaceIndex) {
        if (spaceIndex >= spaceSlots.length) {
            growSpaces(spaceIndex + 1);
        }
        Object[] slot = spaceSlots[spaceIndex];
        if (slot == null) {
            slot = new Object[initialCapacity];
            spaceSlots[spaceIndex] = slot;
        }
        return slot;
    }

    private Object[] grow(Object[] current, int minSize, int spaceIndex) {
        int newSize = Math.max(current.length * 2, minSize);
        Object[] expanded = Arrays.copyOf(current, newSize);
        spaceSlots[spaceIndex] = expanded;
        return expanded;
    }

    private void growSpaces(int minSize) {
        synchronized (this) {
            if (spaceSlots.length >= minSize) {
                return;
            }
            int newSize = Math.max(spaceSlots.length * 2, minSize);
            spaceSlots = Arrays.copyOf(spaceSlots, newSize);
        }
    }

    private int resolveSpaceOrdinal(String spaceName) {
        if (spaceName == null || spaceName.isEmpty()) {
            return 0;
        }
        return spaceOrdinals.computeIfAbsent(spaceName, k -> spaceOrdinals.size());
    }

    private int resolveValueOrdinal(String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        return valueOrdinals.computeIfAbsent(name, k -> nextOrdinal.getAndIncrement());
    }
}

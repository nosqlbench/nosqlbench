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

import java.util.concurrent.ConcurrentHashMap;

/**
 * An extension of ConcurrentIndexCacheWrapper that also provides access to the original key name.
 * This allows Space implementations to be initialized with the original String value used in the wrapper.
 */
public class ConcurrentIndexCacheWrapperWithName {

    private final ConcurrentHashMap<Object, Integer> forwardMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> indexToNameMap = new ConcurrentHashMap<>();

    /**
     * Maps a key to an index and stores the key's string representation for later retrieval.
     *
     * @param key The key to map to an index
     * @return The index for the key
     */
    public int mapKeyToIndex(Object key) {
        Integer index = forwardMap.computeIfAbsent(key, this::nextIndex);
        // Store the key's string representation if it's not already stored
        indexToNameMap.computeIfAbsent(index, idx -> key.toString());
        return index;
    }

    /**
     * Gets the original name (string representation of the key) for a given index.
     *
     * @param index The index to get the name for
     * @return The name associated with the index, or null if not found
     */
    public String getNameForIndex(int index) {
        return indexToNameMap.get(index);
    }

    private int idx = 0;
    private synchronized int nextIndex(Object any) {
        return idx++;
    }
}
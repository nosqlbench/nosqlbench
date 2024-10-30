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


import io.nosqlbench.virtdata.library.basics.shared.functionadapters.ToLongFunction;
import scala.Int;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This is only here for backwards compatibility. If you get a warning that this class is being used,
 * you can probably ignore it for low cardinality of spaces. However, if you want to optimize your
 * client to hold spaces more densely (avoiding the egregious heap overhead of hashing), then modify
 * your space binding to produce a long or int value. In that case, this wrapper type will
 * not be used, and the memory overhead will be minimal.
 */
public class ConcurrentIndexCacheWrapper {

    private ConcurrentHashMap<Object, Integer> forwardMap = new ConcurrentHashMap<>();

    public int mapKeyToIndex(Object key) {
        return forwardMap.computeIfAbsent(key, this::nextIndex);
    }

    private int idx=0;
    private synchronized int nextIndex(Object any) {
        return idx++;
    }

}

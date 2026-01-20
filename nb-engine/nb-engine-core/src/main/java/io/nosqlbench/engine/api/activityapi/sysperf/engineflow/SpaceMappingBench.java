/*
 * Copyright (c) 2025 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.sysperf.engineflow;

import io.nosqlbench.adapters.api.activityimpl.uniform.ConcurrentIndexCacheWrapperWithName;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class SpaceMappingBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"8", "64", "1024"})
        public int keyCount;

        public ConcurrentIndexCacheWrapperWithName cache;
        public String[] keys;
        public int idx;

        @Setup(Level.Trial)
        public void setup() {
            this.cache = new ConcurrentIndexCacheWrapperWithName();
            this.keys = new String[keyCount];
            for (int i = 0; i < keyCount; i++) {
                keys[i] = "space" + i;
                cache.mapKeyToIndex(keys[i]);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.idx = 0;
        }
    }

    @Benchmark
    public void mapExistingKey(BenchState s, Blackhole bh) {
        String key = s.keys[(s.idx++) % s.keyCount];
        int ordinal = s.cache.mapKeyToIndex(key);
        bh.consume(ordinal);
        bh.consume(s.cache.getNameForIndex(ordinal));
    }
}


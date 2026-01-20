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

import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowContext;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class OpFlowContextBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"1", "4", "16"})
        public int fieldCount;

        @Param({"1", "4"})
        public int spaceCount;

        @Param({"ordinal", "name"})
        public String accessMode;

        @Param({"true", "false"})
        public boolean resetEachInvocation;

        public OpFlowContext ctx;
        public String[] fieldNames;
        public int[] spaceIndices;
        public long cycle;
        public Object value;

        @Setup(Level.Trial)
        public void setup() {
            this.ctx = new OpFlowContext(0L, Math.max(16, fieldCount), Math.max(4, spaceCount));
            this.fieldNames = new String[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                fieldNames[i] = "v" + i;
            }
            this.spaceIndices = new int[spaceCount];
            for (int i = 0; i < spaceCount; i++) {
                spaceIndices[i] = i;
            }
            this.value = 1D;

            for (int spaceIndex : spaceIndices) {
                for (String name : fieldNames) {
                    ctx.set(name, null, spaceIndex);
                }
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.cycle = 0L;
        }
    }

    @Benchmark
    public void setGet(BenchState s, Blackhole bh) {
        if (s.resetEachInvocation) {
            s.ctx.reset(s.cycle++);
        }

        if ("ordinal".equals(s.accessMode)) {
            for (int spaceIndex : s.spaceIndices) {
                for (int ordinal = 0; ordinal < s.fieldCount; ordinal++) {
                    s.ctx.set(ordinal, s.value, spaceIndex);
                    bh.consume(s.ctx.get(ordinal, spaceIndex));
                }
            }
        } else {
            for (int spaceIndex : s.spaceIndices) {
                for (String name : s.fieldNames) {
                    s.ctx.set(name, s.value, spaceIndex);
                    bh.consume(s.ctx.get(name, spaceIndex));
                }
            }
        }
    }

    @Benchmark
    public void resetOnly(BenchState s, Blackhole bh) {
        s.ctx.reset(s.cycle++);
        bh.consume(s.ctx.lastCycle());
    }
}


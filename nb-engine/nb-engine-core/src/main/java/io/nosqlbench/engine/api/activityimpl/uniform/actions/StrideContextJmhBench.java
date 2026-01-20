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

package io.nosqlbench.engine.api.activityimpl.uniform.actions;

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
public class StrideContextJmhBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"1", "8", "64", "256"})
        public int strideLength;

        @Param({"1", "4", "16"})
        public int fieldCount;

        @Param({"ordinal", "name"})
        public String accessMode;

        public StrideContext strideContext;
        public String[] fieldNames;
        public long cycle;
        public Object value;

        @Setup(Level.Trial)
        public void setup() {
            this.strideContext = new StrideContext(strideLength, Math.max(16, fieldCount), 4);
            this.value = 1D;
            this.fieldNames = new String[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                fieldNames[i] = "v" + i;
            }

            strideContext.setSpaceIndexResolver(c -> (int) (c & 3L));
            strideContext.setSpaceNameResolver(c -> "space" + (c & 3L));

            OpFlowContext ctx = strideContext.beginCycle(0L);
            for (String name : fieldNames) {
                ctx.set(name, null, 0);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.cycle = 0L;
        }
    }

    @Benchmark
    public void beginCycleAndTouchFlow(BenchState s, Blackhole bh) {
        long cycle = s.cycle++;
        OpFlowContext ctx = s.strideContext.beginCycle(cycle);

        int spaceIndex = s.strideContext.getSpaceIndexResolver().applyAsInt(cycle);
        s.strideContext.setCurrentSpaceIndex(spaceIndex);

        if ("ordinal".equals(s.accessMode)) {
            for (int ordinal = 0; ordinal < s.fieldCount; ordinal++) {
                ctx.set(ordinal, s.value, spaceIndex);
                bh.consume(ctx.get(ordinal, spaceIndex));
            }
        } else {
            for (String name : s.fieldNames) {
                ctx.set(name, s.value, spaceIndex);
                bh.consume(ctx.get(name, spaceIndex));
            }
        }
    }

    @Benchmark
    public void resetResultBufferOnly(BenchState s, Blackhole bh) {
        s.strideContext.resetResultBuffer();
        bh.consume(s.strideContext.resultBuffer().hasRemaining());
    }

    @Benchmark
    public void appendStrideBuffer(BenchState s, Blackhole bh) {
        s.strideContext.resetResultBuffer();
        long start = s.cycle;
        for (int i = 0; i < s.strideLength; i++) {
            s.strideContext.resultBuffer().append(start + i, 0);
        }
        s.cycle += s.strideLength;
        bh.consume(s.strideContext.resultBuffer().hasRemaining());
    }

    @Benchmark
    public void beginCycleThenAppendStride(BenchState s, Blackhole bh) {
        s.strideContext.resetResultBuffer();
        long start = s.cycle;
        for (int i = 0; i < s.strideLength; i++) {
            long cycle = start + i;
            s.strideContext.beginCycle(cycle);
            s.strideContext.resultBuffer().append(cycle, 0);
        }
        s.cycle += s.strideLength;
        bh.consume(s.strideContext.resultBuffer().hasRemaining());
    }
}


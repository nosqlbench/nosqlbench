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
import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowState;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class OpFlowStateBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"1", "8", "64", "256"})
        public int stride;

        @Param({"true", "false"})
        public boolean resetSlot;

        public OpFlowState state;
        public long cycle;

        @Setup(Level.Trial)
        public void setup() {
            this.state = new OpFlowState(stride, 16, 4);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.cycle = 0L;
        }
    }

    @Benchmark
    public void accessSlotAndTouch(BenchState s, Blackhole bh) {
        OpFlowContext ctx = s.resetSlot ? s.state.begin(s.cycle) : s.state.contextFor(s.cycle);
        ctx.set(0, s.cycle);
        bh.consume(ctx.get(0));
        s.cycle++;
    }
}


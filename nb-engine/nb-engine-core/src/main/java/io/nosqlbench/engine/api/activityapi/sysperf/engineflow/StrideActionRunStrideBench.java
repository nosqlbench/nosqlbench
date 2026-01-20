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
import io.nosqlbench.engine.api.activityapi.core.StrideAction;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class StrideActionRunStrideBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"1", "8", "64", "256"})
        public int strideLength;

        @Param({"noop", "flow"})
        public String actionType;

        public StrideAction action;
        public ResettableRangeSegment segment;
        public long baseCycle;

        @Setup(Level.Trial)
        public void setup() {
            this.segment = new ResettableRangeSegment();
            this.action = "flow".equals(actionType) ? new FlowTouchStrideAction(strideLength) : new NoopStrideAction();
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.baseCycle = 0L;
        }
    }

    @Benchmark
    public int runStride(BenchState s, Blackhole bh) {
        s.segment.reset(s.baseCycle, s.strideLength);
        int code = s.action.runStride(s.segment);
        bh.consume(s.segment.peekNextCycle());
        s.baseCycle += s.strideLength;
        return code;
    }

    private static final class NoopStrideAction implements StrideAction {
        @Override
        public int runCycle(long cycle) {
            return 0;
        }
    }

    private static final class FlowTouchStrideAction implements StrideAction {
        private final OpFlowState flowState;

        private FlowTouchStrideAction(int strideLength) {
            this.flowState = new OpFlowState(strideLength, 16, 4);
        }

        @Override
        public int runCycle(long cycle) {
            OpFlowContext ctx = flowState.begin(cycle);
            ctx.set(0, cycle);
            return (ctx.get(0) != null) ? 0 : 1;
        }
    }

    private static final class ResettableRangeSegment implements CycleSegment {
        private long next;
        private long endExclusive;

        private void reset(long startInclusive, int length) {
            this.next = startInclusive;
            this.endExclusive = startInclusive + Math.max(0, length);
        }

        @Override
        public long nextCycle() {
            return (next < endExclusive) ? next++ : -1L;
        }

        @Override
        public long nextRecycle() {
            return nextCycle();
        }

        @Override
        public boolean isExhausted() {
            return next >= endExclusive;
        }

        @Override
        public long peekNextCycle() {
            return (next < endExclusive) ? next : -1L;
        }
    }
}


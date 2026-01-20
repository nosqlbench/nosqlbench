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

import io.nosqlbench.adapters.api.activityimpl.flow.FlowContextAwareOp;
import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowContext;
import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowState;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.CapturingOp;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class CapturingOpFlowPipelineBench {

    @State(Scope.Thread)
    public static class BenchState {
        @Param({"1", "4", "16"})
        public int fieldCount;

        @Param({"1", "8", "64"})
        public int stride;

        @Param({"true", "false"})
        public boolean withFlowContext;

        public OpFlowState flowState;
        public String[] fieldNames;
        public HashMap<String, Object> reusableMap;
        public CapturingOp<HashMap<String, Object>> capturingOp;
        public FlowReadingOp flowReadingOp;
        public long cycle;

        @Setup(Level.Trial)
        public void setup() {
            this.flowState = new OpFlowState(stride, Math.max(16, fieldCount), 4);
            this.fieldNames = new String[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                fieldNames[i] = "v" + i;
            }

            this.reusableMap = new HashMap<>(fieldCount * 2);
            for (String name : fieldNames) {
                reusableMap.put(name, 0D);
            }

            CycleOp<HashMap<String, Object>> sourceOp = c -> {
                for (int i = 0; i < fieldCount; i++) {
                    reusableMap.put(fieldNames[i], (double) (c + i));
                }
                return reusableMap;
            };

            Function<HashMap<String, Object>, Map<String, ?>> extractor = m -> m;
            this.capturingOp = new CapturingOp<>(sourceOp, extractor);
            this.flowReadingOp = new FlowReadingOp(fieldNames);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            this.cycle = 0L;
        }
    }

    @Benchmark
    public void captureThenRead(BenchState s, Blackhole bh) {
        OpFlowContext ctx = s.flowState.begin(s.cycle);

        if (s.withFlowContext) {
            s.capturingOp.setFlowContext(ctx, 0);
            s.flowReadingOp.setFlowContext(ctx, 0);
        } else {
            s.capturingOp.setFlowContext(null, 0);
            s.flowReadingOp.setFlowContext(null, 0);
        }

        Map<String, ?> captured = s.capturingOp.apply(s.cycle);
        bh.consume(captured);
        bh.consume(s.flowReadingOp.apply(s.cycle));
        s.cycle++;
    }

    private static final class FlowReadingOp implements CycleOp<Double>, FlowContextAwareOp {
        private final String[] fieldNames;
        private OpFlowContext flowContext;
        private int spaceIndex;

        private FlowReadingOp(String[] fieldNames) {
            this.fieldNames = fieldNames;
        }

        @Override
        public void setFlowContext(OpFlowContext context, int spaceIndex) {
            this.flowContext = context;
            this.spaceIndex = spaceIndex;
        }

        @Override
        public Double apply(long value) {
            if (flowContext == null) {
                return 0D;
            }
            double sum = 0D;
            for (String name : fieldNames) {
                Object v = flowContext.get(name, spaceIndex);
                if (v instanceof Double d) {
                    sum += d;
                }
            }
            return sum;
        }
    }
}

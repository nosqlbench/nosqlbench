/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.sandbox;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.engine.api.activityapi.simrate.SimRate;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import org.junit.jupiter.api.Disabled;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Group)
@Measurement(time = 10,timeUnit = TimeUnit.SECONDS)
public class SimRateSanityTest {

    private final NBComponent parent = new TestComponent("rltest","rltest");

    public static void main(String[] args) {
        Options jmhOptions = new OptionsBuilder()
//            .include("simrate[0-9]+")
//            .include("simrate(1|24|240)")
            .forks(1)
            .warmupBatchSize(1)
            .warmupIterations(0)
            .build();
        try {
            new Runner(jmhOptions).run();
        } catch (RunnerException e) {
            throw new RuntimeException(e);
        }
    }

    private SimRate rl;

    @Setup
    public void setup() {
        SimRateSpec spec = new SimRateSpec(250,1.01);
        rl = new SimRate(parent,spec);
    }

    @Benchmark
    @Group("at250ops1thread")
    @GroupThreads(1)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void at250ops1thread() {
        rl.block();
    }

    @Benchmark
    @Group("at250ops240threads")
    @GroupThreads(240)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void at250ops240threads() {
        rl.block();
    }

}

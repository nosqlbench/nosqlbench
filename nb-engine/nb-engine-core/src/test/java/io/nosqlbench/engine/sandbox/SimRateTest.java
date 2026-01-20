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
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Tag("microbench")
@Tag("engine")
@State(Scope.Group)
@Measurement(time = 10,timeUnit = TimeUnit.SECONDS)
public class SimRateTest {


    private final NBComponent parent = new TestComponent("rltest","rltest");

    public static void main(String[] args) {
        Options jmhOptions = buildJmhOptions("jmh-simrate.json");
        try {
            new Runner(jmhOptions).run();
        } catch (RunnerException e) {
            throw new RuntimeException(e);
        }
    }

    private SimRate rl;

    @Setup
    public void setup() {
        SimRateSpec spec = new SimRateSpec(1000000000.0,1.1);
        // dang only 250M/s :]
        rl = new SimRate(parent,spec);

    }

    @Benchmark
    @Group("simrate1")
    @GroupThreads(1)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest1() {
        rl.block();
    }

    @Benchmark
    @Group("simrate6")
    @GroupThreads(6)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest6() {
        rl.block();
    }

    @Benchmark
    @Group("simrate12")
    @GroupThreads(12)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest12() {
        rl.block();
    }

    @Benchmark
    @Group("simrate24")
    @GroupThreads(24)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest24() {
        rl.block();
    }

    @Benchmark
    @Group("simrate240")
    @GroupThreads(240)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest240() {
        rl.block();
    }

    @Benchmark
    @Group("simrate2400")
    @GroupThreads(2400)
    @BenchmarkMode(Mode.Throughput)
    @Disabled
    public void tptest2400() {
        rl.block();
    }

    @Test
    @Disabled
    public void testBasicRate() {
        SimRateSpec spec = new SimRateSpec(1000000000.0, 1.1);
        // dang only 250M/s :]
        SimRate rl = new SimRate(parent,spec);
        for (long i = 0; i < 10000000000L; i++) {
            long waiting = rl.block();
            if ((i%100000000)!=0) continue;
            System.out.println("op time:" + i);
        }
    }

    @State(Scope.Group)
    public static class ThreadCountState {
        @Param({"1","12","24"})
        public int threads;
    }

    @Test
    public void runJmhBenchmarks() throws RunnerException {
        new Runner(buildJmhOptions("jmh-simrate.json")).run();
    }

    private static Options buildJmhOptions(String resultFileName) {
        Path resultPath = Path.of("target", resultFileName);
        try {
            Files.createDirectories(resultPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JMH results directory", e);
        }
        return new OptionsBuilder()
            .include(SimRateTest.class.getSimpleName())
//            .include("simrate[0-9]+")
//            .include("simrate(1|24|240)")
            .forks(0)
            .warmupBatchSize(1)
            .warmupIterations(0)
            .resultFormat(ResultFormatType.JSON)
            .result(resultPath.toString())
            .build();
    }

}

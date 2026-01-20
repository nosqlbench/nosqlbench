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

package io.nosqlbench.virtdata.lib.vectors.util;

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
@Tag("variates")
@State(Scope.Benchmark)
public class BitFieldsJMHTest {

    @Param({"1"})
    public int inputs;

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(1)
    @Warmup(iterations=1, timeUnit= TimeUnit.SECONDS,time=10)
    @Measurement(iterations=1,timeUnit=TimeUnit.SECONDS,time=10)
    public void reverseBits1() {
        inputs*=37;
        BitFields.reverseBits1(inputs,32);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(1)
    @Warmup(iterations=1, timeUnit= TimeUnit.SECONDS,time=10)
    @Measurement(iterations=1,timeUnit=TimeUnit.SECONDS,time=10)
    public void reverseBits2() {
        inputs*=37;
        BitFields.reverseBits2(inputs);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(1)
    @Warmup(iterations=1, timeUnit= TimeUnit.SECONDS,time=10)
    @Measurement(iterations=1,timeUnit=TimeUnit.SECONDS,time=10)
    public void reverseBits3() {
        inputs*=37;
        BitFields.reverseBits3(inputs);
    }

    @Test
    public void runJmhBenchmarks() throws RunnerException {
        new Runner(buildJmhOptions("jmh-bitfields.json")).run();
    }

    private static Options buildJmhOptions(String resultFileName) {
        Path resultPath = Path.of("target", resultFileName);
        try {
            Files.createDirectories(resultPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JMH results directory", e);
        }
        return new OptionsBuilder()
            .include(BitFieldsJMHTest.class.getSimpleName())
            .forks(0)
            .resultFormat(ResultFormatType.JSON)
            .result(resultPath.toString())
            .build();
    }

}

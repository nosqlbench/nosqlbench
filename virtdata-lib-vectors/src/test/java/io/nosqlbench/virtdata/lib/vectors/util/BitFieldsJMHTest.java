/*
 * Copyright (c) 2024 nosqlbench
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

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

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


}

/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.sysperf;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class SysBenchMethodNanoTime {
    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(1)
    @Warmup(iterations=1, timeUnit= TimeUnit.SECONDS,time=10)
    @Measurement(iterations=1,timeUnit=TimeUnit.SECONDS,time=10)
    public void callSystemNanoTime(Blackhole spaceTimeSwirls) {
        long t=System.nanoTime();
        spaceTimeSwirls.consume(t);
    }


}

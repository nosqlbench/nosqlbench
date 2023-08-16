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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.testutils.Perf;
import io.nosqlbench.api.testutils.Result;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Function;


public class TestHybridRateLimiterPerf {

    private final Function<RateSpec, RateLimiter> rlFunction = rs -> new HybridRateLimiter(NBLabeledElement.EMPTY,"hybrid", rs.withVerb(RateSpec.Verb.start));
    private final RateLimiterPerfTestMethods methods = new RateLimiterPerfTestMethods();

    @Test
    @Disabled
    public void testPerf1e9() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E9, 1.1),10_000_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e8() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E8, 1.1),50_000_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e7() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E7, 1.1),5_000_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e6() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E6, 1.1),500_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e5() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E5, 1.1),50_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e4() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E4, 1.1),5_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e3() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E3, 1.1),500,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e2() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E2, 1.1),50,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e1() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E1, 1.1),5,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testPerf1e0() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E0, 1.1),2,0.005d);
        System.out.println(result);
    }

    @Test
    @Disabled
    public void testePerf1eN1() {
        final Result result = this.methods.rateLimiterSingleThreadedConvergence(this.rlFunction,new RateSpec(1.0E-1, 1.1),1,0.005d);
        System.out.println(result);

    }

    @Test
    @Disabled
    public void test100Mops_160threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 10_000_000,160);
        System.out.println(perf.getLastResult());
    }

    @Test
    @Disabled
    public void test100Mops_80threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 10_000_000,80);
        System.out.println(perf.getLastResult());
    }

    // 40 threads at 100_000_000 ops/s
    // JVM 1.8.0_152
    // 400000000_ops 29.819737_S 13413934.327_ops_s, 75_ns_op
    // 800000000_ops 60.616158_S 13197801.155_ops_s, 76_ns_op
    // JVM 11.0.1
    // 400000000_ops 33.622751_S 11896706.363_ops_s, 84_ns_op
    @Test
    @Disabled
    public void test100Mops_40threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 10_000_000,40);
        System.out.println(perf.getLastResult());
    }

    // 20 threads at 100_000_000 ops/s
    // JVM 1.8.0_152
    // 200000000_ops 14.031716_S 14253424.087_ops_s, 70_ns_op
    // 400000000_ops 35.918071_S 11136455.474_ops_s, 90_ns_op
    // 400000000_ops 30.809579_S 12982975.401_ops_s, 77_ns_op
    // 400000000_ops 36.985547_S 10815035.410_ops_s, 92_ns_op
    // 200000000_ops 16.843876_S 11873751.403_ops_s, 84_ns_op
    // 200000000_ops 17.382563_S 11505783.253_ops_s, 87_ns_op
    // JVM 11.0.1
    // 200000000_ops 12.247201_S 16330261.978_ops_s, 61_ns_op
    // 200000000_ops 15.915484_S 12566379.106_ops_s, 80_ns_op
    // 200000000_ops 17.691698_S 11304737.461_ops_s, 88_ns_op

    @Test
    @Disabled
    public void test100Mops_20threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 10_000_000,20);
        System.out.println(perf.getLastResult());
    }

    // 10 threads at 100_000_000 ops/s
    // JVM 1.8.0_152
    // 100000000_ops 5.369642_S 18623216.864_ops_s, 54_ns_op
    // 200000000_ops 16.744912_S 11943926.287_ops_s, 84_ns_op
    // 200000000_ops 17.474475_S 11445264.894_ops_s, 87_ns_op
    // 200000000_ops 14.089247_S 14195222.897_ops_s, 70_ns_op
    @Test
    @Disabled
    public void test100Mops_10threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 10_000_000,10);
        System.out.println(perf.getLastResult());
    }

    // 5 threads at 100_000_000 ops/s
    // JVM 1.8.0_152
    // 50000000_ops 2.477219_S 20183923.068_ops_s, 50_ns_op
    // 200000000_ops 10.422393_S 19189451.478_ops_s, 52_ns_op
    // 200000000_ops 10.624822_S 18823844.646_ops_s, 53_ns_op
    // JVM 11.0.1
    // 200000000_ops 11.839666_S 16892368.438_ops_s, 59_ns_op
    @Test
    @Disabled
    public void test100Mops_5threads() {
        final Perf perf = this.methods.testRateLimiterMultiThreadedContention(this.rlFunction, new RateSpec(1.0E8, 1.1), 40_000_000,5);
        System.out.println(perf.getLastResult());
    }

}

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
import io.nosqlbench.api.testutils.Result;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

/**
 * These tests are for sanity checking rate limiter implementations. They are not enabled by default,
 * since they are very CPU an time intensive. If you are developing rate limiters, use these to understand
 * throughput variations at different speeds and different levels of contention.
 *
 *  This set is for single-threaded (uncontended) baselines, at different op rates.
 */
public class TestRateLimiterPerfSingle {

    private final Function<RateSpec, RateLimiter> rlFunction = rs -> new HybridRateLimiter(NBLabeledElement.forKV("alias","tokenrl"),"hybrid", rs.withVerb(RateSpec.Verb.start));
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

}

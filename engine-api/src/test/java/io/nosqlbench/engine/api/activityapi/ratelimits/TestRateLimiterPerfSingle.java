/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityapi.ratelimits;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.testutils.Result;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Function;

/**
 * These tests are for sanity checking rate limiter implementations. They are not enabled by default,
 * since they are very CPU an time intensive. If you are developing rate limiters, use these to understand
 * throughput variations at different speeds and different levels of contention.
 *
 *  This set is for single-threaded (uncontended) baselines, at different op rates.
 */
public class TestRateLimiterPerfSingle {

    private Function<RateSpec, RateLimiter> rlFunction = rs -> new HybridRateLimiter(ActivityDef.parseActivityDef("alias=tokenrl"),"hybrid", rs.withVerb(RateSpec.Verb.start));
    private RateLimiterPerfTestMethods methods = new RateLimiterPerfTestMethods();

    @Test
    @Ignore
    public void testPerf1e9() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E9, 1.1),10_000_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e8() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E8, 1.1),50_000_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e7() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E7, 1.1),5_000_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e6() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E6, 1.1),500_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e5() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E5, 1.1),50_000,0.01d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e4() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E4, 1.1),5_000,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e3() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E3, 1.1),500,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e2() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E2, 1.1),50,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e1() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E1, 1.1),5,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testPerf1e0() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E0, 1.1),2,0.005d);
        System.out.println(result);
    }

    @Test
    @Ignore
    public void testePerf1eN1() {
        Result result = methods.rateLimiterSingleThreadedConvergence(rlFunction,new RateSpec(1E-1, 1.1),1,0.005d);
        System.out.println(result);

    }

}

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

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenPoolTest {

    @Test
    public void testBackfillFullRate() {
        TokenPool p = new TokenPool(100, 1.1);
        assertThat(p.refill(100L)).isEqualTo(100L);
        assertThat(p.getWaitPool()).isEqualTo(0L);
        assertThat(p.refill(100L)).isEqualTo(200);
        assertThat(p.getWaitPool()).isEqualTo(90L);
        assertThat(p.refill(10L)).isEqualTo(210L);
        assertThat(p.getWaitPool()).isEqualTo(100L);

        assertThat(p.refill(10)).isEqualTo(220L);
        assertThat(p.takeUpTo(100)).isEqualTo(100L);

    }
    @Test
    public void testTakeRanges() {
        TokenPool p = new TokenPool(100, 10);
        p.refill(100);
        assertThat(p.takeUpTo(99)).isEqualTo(99L);
        assertThat(p.takeUpTo(10)).isEqualTo(1L);
        assertThat(p.takeUpTo(1L)).isEqualTo(0L);
    }

    @Test
    public void testChangedParameters() {

        RateSpec s1 = new RateSpec(1000L, 1.10D);
        TokenPool p = new TokenPool(s1);
        long r = p.refill(10000000);
        assertThat(r).isEqualTo(10000000L);
        assertThat(p.getWaitTime()).isEqualTo(10000000L);

        RateSpec s2 = new RateSpec(1000000L, 1.10D);
        p.apply(s2);


    }
}

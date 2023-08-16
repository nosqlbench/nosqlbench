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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RateSpecTest {

    @Test
    public void testDefaultRateSpecPattern() {
        RateSpec r = new RateSpec("523");
        assertThat(r.getRate()).isEqualTo(523.0d);
        assertThat(r.getBurstRatio()).isEqualTo(1.1d);
    }

    @Test
    public void testBurstRatioPattern() {
        RateSpec r = new RateSpec("12345,1.3");
        assertThat(r.getRate()).isEqualTo(12345.0d);
        assertThat(r.getBurstRatio()).isEqualTo(1.3d);
    }

    @Test
    public void testTypeSelection() {
        RateSpec a = new RateSpec("12345,1.4,configure");
        assertThat(a.getVerb()).isEqualTo(RateSpec.Verb.configure);
        RateSpec d = new RateSpec("12345,1.4,restart");
        assertThat(d.verb).isEqualTo(RateSpec.Verb.restart);
        RateSpec c = new RateSpec("12345,1.1");
        assertThat(c.verb).isEqualTo(RateSpec.Verb.start);
    }
}

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

import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimRateSpecTest {

    @Test
    public void testDefaultRateSpecPattern() {
        SimRateSpec r = new SimRateSpec("523");
        assertThat(r.getRate()).isEqualTo(523.0d);
        assertThat(r.getBurstRatio()).isEqualTo(1.1d);
    }

    @Test
    public void testBurstRatioPattern() {
        SimRateSpec r = new SimRateSpec("12345,1.3");
        assertThat(r.getRate()).isEqualTo(12345.0d);
        assertThat(r.getBurstRatio()).isEqualTo(1.3d);
    }

    @Test
    public void testTypeSelection() {
        SimRateSpec a = new SimRateSpec("12345,1.4,configure");
        assertThat(a.getVerb()).isEqualTo(SimRateSpec.Verb.configure);
        SimRateSpec d = new SimRateSpec("12345,1.4,restart");
        assertThat(d.verb).isEqualTo(SimRateSpec.Verb.restart);
        SimRateSpec c = new SimRateSpec("12345,1.1");
        assertThat(c.verb).isEqualTo(SimRateSpec.Verb.start);
    }

    @Test
    public void testVariantFormats() {
        assertThat(new SimRateSpec("12345"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.1d, SimRateSpec.Verb.start, SimRateSpec.Scope.activity
                ));
        assertThat(new SimRateSpec("12345,1.4"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.4d, SimRateSpec.Verb.start, SimRateSpec.Scope.activity
                ));
        assertThat(new SimRateSpec("12345,configure"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.1d, SimRateSpec.Verb.configure, SimRateSpec.Scope.activity
                ));
        assertThat(new SimRateSpec("12345,thread"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.1d, SimRateSpec.Verb.start, SimRateSpec.Scope.thread
                ));
        assertThat(new SimRateSpec("12345,1.4,thread"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.4d, SimRateSpec.Verb.start, SimRateSpec.Scope.thread
                ));
        assertThat(new SimRateSpec("12345,configure,activity"))
                .isEqualTo(new SimRateSpec(
                        12345.0d, 1.1d, SimRateSpec.Verb.configure, SimRateSpec.Scope.activity
                ));

    }
}

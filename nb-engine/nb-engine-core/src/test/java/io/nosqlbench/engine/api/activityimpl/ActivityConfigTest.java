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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityConfigTest {

    @Test
    public void testSimpleCycleCount() {
        ActivityConfig config = Activity.configFor("cycles=1M");
        assertThat(config.getCyclesSpec().firstSpec()).isEqualTo(0L);
        assertThat(config.getCyclesSpec().last_exclusive()).isEqualTo(1000000L);
    }

    @Test
    public void testCycleRange() {
        ActivityConfig config = Activity.configFor("cycles=1M..5M");
        assertThat(config.getCyclesSpec().firstSpec()).isEqualTo(1000000L);
        assertThat(config.getCyclesSpec().last_exclusive()).isEqualTo(5000000L);
    }

}

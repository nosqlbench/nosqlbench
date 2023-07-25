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

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityDefTest {

    @Test
    public void testSimpleCycleCount() {
        ActivityDef d = ActivityDef.parseActivityDef("cycles=1M");
        assertThat(d.getStartCycle()).isEqualTo(0);
        assertThat(d.getEndCycle()).isEqualTo(1000000);
    }

    @Test
    public void testCycleRange() {
        ActivityDef d = ActivityDef.parseActivityDef("cycles=1M..5M");
        assertThat(d.getStartCycle()).isEqualTo(1000000);
        assertThat(d.getEndCycle()).isEqualTo(5000000);
        assertThat(d.getCycleCount()).isEqualTo(4000000);
    }

}

/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl.motor;

import io.nosqlbench.engine.api.activityapi.core.RunState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RunStateImageTest {

    @Test
    public void testMaxStateImage() {
        int[] counts = new int[RunState.values().length];
        counts[RunState.Running.ordinal()]=3;
        RunStateImage image = new RunStateImage(counts, false);
        assertThat(image.is(RunState.Running)).isTrue();
        assertThat(image.isTimeout()).isFalse();
        assertThat(image.is(RunState.Errored)).isFalse();
        assertThat(image.isOnly(RunState.Running)).isTrue();
        RunState maxState = image.getMaxState();
        assertThat(maxState).isEqualTo(RunState.values()[2]);
    }

}

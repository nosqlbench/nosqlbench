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

package io.nosqlbench.engine.api.activityimpl.tracking;

import io.nosqlbench.engine.api.activityimpl.marker.longheap.TreeTracker1024;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeTracker1024Test {

    @Test
    public void testFullCompletion() {
        TreeTracker1024 tt = new TreeTracker1024(0L);
        assertThat(tt.isCompleted()).isFalse().as("initial state");
        for(long i=0;i<1022;i++) {
            tt.setPosition(i);
            assertThat(tt.isCompleted()).isFalse().as("cycle: {}",i);
        }
        tt.setPosition(1023);
//        assertThat(tt.isCycleCompleted()).isTrue().as("cycle: 1023");
    }
}

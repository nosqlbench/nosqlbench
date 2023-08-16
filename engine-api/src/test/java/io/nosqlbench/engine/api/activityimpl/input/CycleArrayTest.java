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

package io.nosqlbench.engine.api.activityimpl.input;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleArray;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleArrayTest {

    @Test
    public void testBasicArray() {

        CycleArray a1 = new CycleArray(2,3,9);

        CycleSegment s1 = a1.getInputSegment(1);
        assertThat(s1).isNotNull();
        assertThat(s1.isExhausted()).isFalse();
        long v1 = s1.nextCycle();
        assertThat(v1).isEqualTo(2L);
        assertThat(s1.isExhausted()).isTrue();
        long v2 = s1.nextCycle();
        assertThat(v2).isLessThan(0);


        CycleSegment s2 = a1.getInputSegment(2);
        assertThat(s2).isNotNull();
        assertThat(s2.isExhausted()).isFalse();
        long v3 = s2.nextCycle();
        assertThat(v3).isEqualTo(3L);
        assertThat(s2.isExhausted()).isFalse();
        long v4 = s2.nextCycle();
        assertThat(v4).isEqualTo(9L);
        assertThat(s2.isExhausted()).isTrue();
        long v5 = s2.nextCycle();
        assertThat(v5).isLessThan(0);

        CycleSegment s3 = a1.getInputSegment(1);
        assertThat(s3).isNull();
    }

}

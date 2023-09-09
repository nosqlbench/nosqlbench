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

import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicInputTest {

    @Test
    public void testThatNoCyclesAndNoRecyclesMeansZero() {
        AtomicInput input = new AtomicInput(NBLabeledElement.EMPTY, ActivityDef.parseActivityDef("alias=foo;cycles=0;recycles=0"));
        CycleSegment inputSegment = input.getInputSegment(1);
        assertThat(inputSegment).isNull();
    }

    @Test
    public void testThatNoCyclesAndDefaultRecyclesMeans1xCycles() {
        AtomicInput input = new AtomicInput(NBLabeledElement.EMPTY, ActivityDef.parseActivityDef("alias=foo;cycles=10"));
        CycleSegment inputSegment =null;

        inputSegment= input.getInputSegment(10);
        assertThat(inputSegment).isNotNull();
        assertThat(inputSegment.nextCycle()).isEqualTo(0L);

        inputSegment = input.getInputSegment(10);
        assertThat(inputSegment).isNull();
    }

    @Test
    public void testThatRadixStackingIsAccurate() {
        int intendedCycles=40;
        int intendedRecycles=4;
        int stride=10;

        AtomicInput input = new AtomicInput(NBLabeledElement.EMPTY, ActivityDef.parseActivityDef("alias=foo;cycles="+intendedCycles+";recycles="+intendedRecycles));
        CycleSegment segment =null;
        for (int nextRecycle = 0; nextRecycle < intendedRecycles; nextRecycle++) {
            for (int nextCycle = 0; nextCycle < intendedCycles; nextCycle+=stride) {
                segment = input.getInputSegment(stride);
                assertThat(segment.nextCycle()).isEqualTo(nextCycle);
                assertThat(segment.nextRecycle()).isEqualTo(nextRecycle);
            }
        }
        segment=input.getInputSegment(stride);
        assertThat(segment).isNull();
    }

    @Test
    public void testThatCycleAndRecycleOffsetsWork() {
        AtomicInput input = new AtomicInput(NBLabeledElement.EMPTY, ActivityDef.parseActivityDef("alias=foo;cycles=310..330;recycles=37..39"));
        CycleSegment segment = null;
        int stride=10;
        segment = input.getInputSegment(stride);
        assertThat(segment.nextCycle()).isEqualTo(310L);
        assertThat(segment.nextRecycle()).isEqualTo(37L);
        segment = input.getInputSegment(stride);
        assertThat(segment.nextCycle()).isEqualTo(320L);
        assertThat(segment.nextRecycle()).isEqualTo(37L);
        segment = input.getInputSegment(stride);
        assertThat(segment.nextCycle()).isEqualTo(310L);
        assertThat(segment.nextRecycle()).isEqualTo(38L);
        segment = input.getInputSegment(stride);
        assertThat(segment.nextCycle()).isEqualTo(320L);
        assertThat(segment.nextRecycle()).isEqualTo(38L);
        segment = input.getInputSegment(stride);
        assertThat(segment).isNull();

    }

    @Test
    public void testEmptyIntervalShouldNotProvideValues() {
        AtomicInput i = new AtomicInput(NBLabeledElement.EMPTY,ActivityDef.parseActivityDef("alias=foo;cycles=23..23"));
        CycleSegment inputSegment = i.getInputSegment(1);
        assertThat(inputSegment).isNull();
    }
}

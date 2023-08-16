/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapters.api.evalcontext;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CompoundCycleFunctionTest {

    private final GroovyCycleFunction<Boolean> truthy = new GroovyCycleFunction<>("truthy", "true;", Map.of(), List.of());
    private final GroovyCycleFunction<Boolean> falsy = new GroovyCycleFunction<>("falsy", "false;", Map.of(), List.of());

    @Test
    public void testReducerFirstOrLastResult() {
        CompoundCycleFunction<Boolean> justB = new CompoundCycleFunction<>((a, b) -> b, truthy, falsy);
        assertThat(justB.apply(2)).isEqualTo(false);

        CompoundCycleFunction<Boolean> justA = new CompoundCycleFunction<>((a, b) -> a, truthy, falsy);
        assertThat(justA.apply(2)).isEqualTo(true);

    }

    @Test
    public void testReducerAnyTrue() {
        CompoundCycleFunction<Boolean> trueOrFalse = new CompoundCycleFunction<>((a, b) -> a||b, truthy, falsy);
        assertThat(trueOrFalse.apply(2)).isEqualTo(true);

        CompoundCycleFunction<Boolean> falseOrFalse = new CompoundCycleFunction<>((a, b) -> a||b, falsy, falsy);
        assertThat(falseOrFalse.apply(2)).isEqualTo(false);

    }

    @Test
    public void testReducerAllTrue() {
        CompoundCycleFunction<Boolean> trueAndFalse = new CompoundCycleFunction<>((a, b) -> a&&b, truthy, falsy);
        assertThat(trueAndFalse.apply(2)).isEqualTo(false);

        CompoundCycleFunction<Boolean> trueAndTrue = new CompoundCycleFunction<>((a, b) -> a&&b, truthy, truthy);
        assertThat(trueAndTrue.apply(2)).isEqualTo(true);

    }





}

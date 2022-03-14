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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SetFunctionsTest {

    @Test
    public void testSetFunctions() {
        SetFunctions f1 = new SetFunctions((LongUnaryOperator) i -> i, (LongFunction<Double>) j -> (double) j);
        Set<Object> set = f1.apply(3);
        System.out.println(set);
    }

    @Test
    public void testSetHashed() {
        SetHashed f1 = new SetHashed((DoubleUnaryOperator) i -> i, (DoubleToLongFunction) i -> (long) i);
        Set<Object> set = f1.apply(2L);
        assertThat(set).contains(8.2188818279493642E18, 3417914777143645696L);
    }

    @Test
    public void testSetSized() {
        SetSized f1 = new SetSized((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("3"); // This is because there is no stepping on SetSized
    }

    @Test
    public void testSetSizedHashed() {
        SetSizedHashed f1 = new SetSizedHashed((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("860564144","1556714733", "745054359");
    }

    @Test
    public void testSetSizedStepped() {
        SetSizedStepped f1 = new SetSizedStepped((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("3","4","5");
    }

    @Test
    public void testStepped() {
        SetStepped f1 = new SetStepped((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("4",3);
        //This is because there is no sizing function. Both functions are value functions
        //And whatever type they produce is put into the set of objects
    }

}

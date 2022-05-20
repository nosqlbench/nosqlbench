package io.nosqlbench.virtdata.core;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.core.bindings.ResolvedFunction;
import org.junit.jupiter.api.Test;

import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedFunctionTest {

    @Test
    public void testToStringWithVarArgs() {
        try {
            TestAdd testAdd = new TestAdd(1, 2, 3);
            Class<?>[] parameterTypes = TestAdd.class.getConstructor(int.class, int[].class).getParameterTypes();
            ResolvedFunction rf = new ResolvedFunction(testAdd, true, parameterTypes, new Object[]{1, 2, 3}, long.class, long.class);
            assertThat(rf.toString()).isEqualTo("long->io.nosqlbench.virtdata.core.ResolvedFunctionTest$TestAdd->long [Integer=>int,Integer...=>int...]");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToStringWithEmptyVarArgs() {
        try {
            TestAdd testAdd = new TestAdd(1);
            Class<?>[] parameterTypes = TestAdd.class.getConstructor(int.class, int[].class).getParameterTypes();
            ResolvedFunction rf = new ResolvedFunction(testAdd, true, parameterTypes, new Object[]{1, 2, 3}, long.class, long.class);
            assertThat(rf.toString()).isEqualTo("long->io.nosqlbench.virtdata.core.ResolvedFunctionTest$TestAdd->long [Integer=>int,Integer...=>int...]");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final static class TestAdd implements LongUnaryOperator {

        private final int a;
        private final int[] b;

        public TestAdd(int a, int... b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public long applyAsLong(long operand) {
            return a + operand;
        }
    }
}

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

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


import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class ToStringTest {

    @Test
    public void testNoArgs() {
        io.nosqlbench.virtdata.library.basics.shared.unary_string.ToString t1 = new io.nosqlbench.virtdata.library.basics.shared.unary_string.ToString();
        assertThat(t1.apply(1L)).isEqualTo("1");
    }

    @Test
    public void testWithLongUnaryOperator() {
        io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString t = new io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString(new LongUnaryOperatorIdentity());
        assertThat(t.apply(2L)).isEqualTo("2");
    }

    @Test
    public void testWithLongFunction() {
        io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString t = new io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString(new LongFuncIdentity());
        assertThat(t.apply(3L)).isEqualTo("3");
    }

    @Test
    public void testWithLongObFunc() {
        io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString t = new io.nosqlbench.virtdata.library.basics.shared.conversions.from_long.ToString(new LongObFunc());
        assertThat(t.apply(4L)).isEqualTo("4");
    }

    private static class LongObFunc implements Function<Long,Object> {

        @Override
        public Object apply(Long aLong) {
            return aLong;
        }
    }
    private static class LongUnaryOperatorIdentity implements LongUnaryOperator {
        @Override
        public long applyAsLong(long operand) {
            return operand;
        }
    }

    private static class LongFuncIdentity implements LongFunction<Long> {
        @Override
        public Long apply(long value) {
            return value;
        }
    }



}

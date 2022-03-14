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

package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FunctionAssemblerTest {

    @Test
    public void testLongUnary() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(5);
    }

    @Test
    public void testLongUnaryLongUnary() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        fass.andThen(new IdentityOperator());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(5);
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void testLongFunction() throws Exception {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(10);

    }

    @Test
    public void testLongFunctionLongFunctionProper() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new LongAddFiveFunction());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(15);
    }

    @Test
    public void testLongFunctionLongFunctionMistyped() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new GenericStringCat());
        DataMapper<String> dataMapper = fass.getDataMapper();
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> dataMapper.get(5));
    }

    @Test
    public void testAndThenFunction() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new GenericLongToString());
        DataMapper<String> dataMapper = fass.getDataMapper();
        String s = dataMapper.get(5);
        assertThat(s).isEqualTo("5");
    }

//    @Test
//    public void testFunctionFunctionProper() {
//        FunctionComposer fass = new FunctionAssembly();
//        fass.andThen(new GenericLongToString());
//        fass.andThen(new GenericStringCat());
//        DataMapper<String> dataMapper = fass.getDataMapper();
//        String s = dataMapper.get(5);
//        assertThat(s).isEqualTo("Cat5");
//    }

    @Test
    public void testFunctionFunctionMistyped() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new GenericStringCat());
        DataMapper<String> dataMapper = fass.getDataMapper();
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> dataMapper.get(5));
    }

    @Test
    public void testLongUnaryLongFunctionFunctionProper() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new GenericLongToString());
        DataMapper<String> dataMapper = fass.getDataMapper();
        String s = dataMapper.get(5);
        assertThat(s).isEqualTo("10");

    }

    private static class IdentityOperator implements LongUnaryOperator {
        @Override
        public long applyAsLong(long operand) {
            return operand;
        }
    }

    private static class LongAddFiveFunction implements LongFunction<Long> {
        @Override
        public Long apply(long value) {
            return value + 5;
        }
    }

    private static class GenericLongToString implements Function<Long,String> {
        @Override
        public String apply(Long aLong) {
            return String.valueOf(aLong);
        }
    }

    private static class GenericStringCat implements Function<String,String> {
        @Override
        public String apply(String s) {
            return "Cat" + s;
        }
    }

}

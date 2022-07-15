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

package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.api.config.standard.NBTypeConverter;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class NBTypeConverterTest {

    @Test
    public void testBasicConversion() {
        BigDecimal value = NBTypeConverter.convert("234323433.22", BigDecimal.class);
        assertThat(value).isEqualTo(BigDecimal.valueOf(234323433.22d));
    }

    @Test
    public void testCoreTypeClosure() {
        for (Class<?> inc : NBTypeConverter.CORE_TYPES) {
            for (Class<?> outc : NBTypeConverter.CORE_TYPES) {
                Object in = genElement(inc);
                System.out.print("inc:" + inc.getSimpleName() + ", outc:" + outc.getSimpleName() +", in:" + in + " --> ");
                assertThat(NBTypeConverter.canConvert(in,outc)).as("Should be able to convert core types from " + inc.getSimpleName() + " to " + outc);

                Object out = NBTypeConverter.convert(in, outc);
                System.out.println("out:" + out +", type:" + out.getClass().getSimpleName());
                assertThat(ClassUtils.isAssignable(out.getClass(),outc,true))
                    .as(outc.getSimpleName() + " should be assignable from "+ out.getClass().getSimpleName())
                    .isTrue();
            }
        }
        System.out.println();
    }

    @Test
    public void testNumberToPrimitiveInterop() {
        String s = NBTypeConverter.convert(Character.valueOf('1'),String.class);
        Character cb = NBTypeConverter.convert(7,Character.class);

        Short b = NBTypeConverter.convert(3,Short.class);
        short a = NBTypeConverter.convert(3,short.class);
        short c = NBTypeConverter.convert(Integer.valueOf(3),short.class);
        Short d = NBTypeConverter.convert(Integer.valueOf(3),Short.class);
    }

    @Test
    public void testAssignables() {
//        long l3 = (int) 3;
//        int i3 = (long) 3l;

        assertThat(long.class.isAssignableFrom(Long.class)).isFalse();
        assertThat(Long.class.isAssignableFrom(long.class)).isFalse();

        assertThat(long.class.isAssignableFrom(long.class)).isTrue();
        assertThat(Long.class.isAssignableFrom(Long.class)).isTrue();
    }
//    @Test
//    public void testUnboxing() {
//        Double d = (Double) (Integer) 3;
//        Object o = NBTypeConverter.adapt(5,double.class);
//        double v = NBTypeConverter.adapt(5,double.class);
//    }

    public Object genElement(Class<?> type) {
        String typeName = type.getSimpleName();
        switch (typeName) {
            case "byte":
                return 1;
            case "Byte":
                return Byte.valueOf("2");
            case "short":
                return 3;
            case "Short":
                return Short.valueOf("4");
            case "int":
                return 5;
            case "Integer":
                return Integer.valueOf("6");
            case "long":
                return 7L;
            case "Long":
                return Long.valueOf(8L);
            case "float":
                return 9.0f;
            case "Float":
                return Float.valueOf(9.1f);
            case "double":
                return 10.0d;
            case "Double":
                return Double.valueOf(10.1d);
            case "Character":
                return Character.valueOf('c');
            case "char":
                return 'c';
            case "String":
                return "1";
            default:
                throw new RuntimeException("Unknown type:" + typeName);

        }
    }
}

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

package io.nosqlbench.virtdata.api.bindings;

import org.junit.jupiter.api.Test;

import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test asserts that all numeric functional types can be converted to another form
 * with internal roll-over logic.
 *
 * Some of these are comment out as they are non-sensical in most cases, but they have been left
 * here as an illustration of the coverage.
 */
public class VirtDataConversionsTest {


    private final static DoubleUnaryOperator duo = i -> i;
    private final static DoubleToLongFunction d2lf = i -> (long) i;
//    private final static DoubleToIntFunction d2if = i -> (int) i;

    private final static LongUnaryOperator luo = i -> i;
    private final static LongToIntFunction l2if = i -> (int) i%Integer.MAX_VALUE;
    private final static LongToDoubleFunction l2df = i -> i;

    private final static IntUnaryOperator iuo = i -> i;
//    private final static IntToLongFunction i2lf = i -> (long) i;
//    private final static IntToDoubleFunction i2df = i -> (double) i;


    private final static LongFunction<Long> lf_l = i -> i;
    private final static LongFunction<Double> lf_d = i -> (double) i;
    private final static LongFunction<Integer> lf_i = i -> (int) i;
    private final static LongFunction<String> lf_s = String::valueOf;
    private final static LongFunction<Object> lf_o = i -> (Object) i;

//    private final static IntFunction<Long> if_l = i -> (long)i;
    private final static IntFunction<Integer> if_i = i -> i;
    private final static IntFunction<Double> if_d = i -> (double) i;
    private final static IntFunction<String> if_s = String::valueOf;
    private final static IntFunction<Object> if_o = i -> (Object) i;

    private final static DoubleFunction<Double> df_d = i -> i;
//    private final static DoubleFunction<Long> df_l = i -> (long)i;
//    private final static DoubleFunction<Integer> df_i = i -> (int)i;
    private final static DoubleFunction<String> df_s = String::valueOf;
    private final static DoubleFunction<Object> df_o = i -> (Object) i;

//    private final static Function<Long,Long> f_l_l = i -> i;
//    private final static Function<Double,Integer> f_d_i = i ->(int)i.doubleValue();
//    private final static Function<Integer,Long> f_i_l = i -> (long)i;
//    private final static Function<Long,Integer> f_l_i = i -> (int)i.longValue();


    private final static Object[] funcs = {
            luo, d2lf, l2df, l2if,
            duo, iuo,

            lf_l, lf_d, lf_i, lf_s, lf_o,

            if_i, if_d, if_s, if_o,

            df_d, df_s, df_o


//            lf_s, lf_d, l2df, l2if, luo, if_i, if_d, iuo, duo,

            // DoubleToInt functions are not supported for now.
            // It doesn't make much sense to add them.
//            d2if,
//            d2lf,

//            df_d,
//            i2df,
//            i2lf
//            f_l_l,
//            f_d_i,
//            f_i_l,
//            f_l_i};

    };
    private final static Object[][] targets = {
            {LongToDoubleFunction.class, int.class},
            {LongToDoubleFunction.class, long.class},
            {LongToDoubleFunction.class, double.class}
    };

    @Test
    public void testFunctionConversions() {
        for (Object func : funcs) {
            LongUnaryOperator f1 = VirtDataConversions.adaptFunction(func, LongUnaryOperator.class);
            f1.applyAsLong(1);

            LongToDoubleFunction f2 = VirtDataConversions.adaptFunction(func, LongToDoubleFunction.class);
            f2.applyAsDouble(2);

            LongToIntFunction f3 = VirtDataConversions.adaptFunction(func, LongToIntFunction.class);
            f3.applyAsInt(3);

            LongFunction<Double> f4 = VirtDataConversions.adaptFunction(func, LongFunction.class, Double.class);
            f4.apply(4L);

            LongFunction<Object> f41 = VirtDataConversions.adaptFunction(func, LongFunction.class, Object.class);
            f41.apply(41L);

            LongFunction<Integer> f5 = VirtDataConversions.adaptFunction(func, LongFunction.class, Integer.class);
            f5.apply(5L);

            IntUnaryOperator f6 = VirtDataConversions.adaptFunction(func, IntUnaryOperator.class);
            f6.applyAsInt(6);

            IntFunction<Long> f7 = VirtDataConversions.adaptFunction(func, IntFunction.class, Long.class);
            f7.apply(7);

            IntFunction<Integer> f8 = VirtDataConversions.adaptFunction(func,IntFunction.class, Integer.class);
            f8.apply(8);

            IntFunction<Double> f9 = VirtDataConversions.adaptFunction(func,IntFunction.class, Double.class);
            f9.apply(9);

            DoubleUnaryOperator f10 = VirtDataConversions.adaptFunction(func, DoubleUnaryOperator.class);
            f10.applyAsDouble(10d);

            LongFunction<Long> f11 = VirtDataConversions.adaptFunction(func, LongFunction.class, Long.class);
            f11.apply(11L);

        }
    }

}

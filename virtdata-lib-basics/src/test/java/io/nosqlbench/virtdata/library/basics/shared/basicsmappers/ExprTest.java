package io.nosqlbench.virtdata.library.basics.shared.basicsmappers;

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


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExprTest {

    @Test
    public void testLongUnaryExpr() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Expr mod5 =
                new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Expr("cycle % 5");
        long three = mod5.applyAsLong(23);
        assertThat(three).isEqualTo(3);
    }

    @Test
    public void testDoubleUnaryExpr() {
        io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Expr plus3point5 =
                new io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Expr("cycle + 3.5");
        double r = plus3point5.applyAsDouble(32.5);
        assertThat(r).isCloseTo(36.0, Offset.offset(0.001d));
    }

    @Test
    public void testLongToIntExpr() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Expr minus7 =
                new io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Expr("cycle - 7");
        int r = minus7.applyAsInt(234233);
        assertThat(r).isEqualTo(234226);
    }

    @Test
    public void testUnaryIntExpr() {
        io.nosqlbench.virtdata.library.basics.shared.unary_int.Expr times2 =
                new io.nosqlbench.virtdata.library.basics.shared.unary_int.Expr("cycle * 2");
        int fourtytwo = times2.applyAsInt(21);
        assertThat(fourtytwo).isEqualTo(42);
    }

    @Test
    public void testLongExprSpeed() {
        //Expr mod5 = new Expr("(cycle / 10)*10 + (cycle % 5)");
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Expr mod5 =
                new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Expr("(cycle / 10)*10 + (cycle % 5)");
        long three = mod5.applyAsLong(23);
        long start = System.nanoTime();
        int min=0;
        int max=1000000;
        for (int i = min; i < max; i++) {
            long l = mod5.applyAsLong(i);
            //System.out.format("input=%d output=%d\n", i, l);
            //assertThat(l).isEqualTo((i%5));

        }
        long end = System.nanoTime();
        long duration = end-start;
        double nsperop = (double) duration / (double) (max-min);

        System.out.format("(ops/time)=(%d/%dns) rate=%.3f\n", (max-min), duration, ((double) max-min)*1000000000.0/duration);
    }

}

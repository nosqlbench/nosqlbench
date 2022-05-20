package io.nosqlbench.virtdata.library.basics.shared.from_double.to_bigdecimal;

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

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;

public class ToBigDecimalTest {

    @Test
    public void demonstrateDoubleToBigDecimal() {
        double big = 1234567890.098765d;
        System.out.println(big);

        ToBigDecimal unlimited = new ToBigDecimal();
        BigDecimal bignum = unlimited.apply(big);
        assertThat(bignum.doubleValue()).isCloseTo(big, Offset.offset(0.000001d));
        assertThat(bignum).isEqualTo(new BigDecimal(big, MathContext.UNLIMITED));

        ToBigDecimal p5rounded = new ToBigDecimal("precision=5 roundingMode=UP");
        BigDecimal rounded = p5rounded.apply(big);
        assertThat(rounded.doubleValue()).isCloseTo(1234600000.0D,Offset.offset(0.0000001d));
        System.out.println(rounded);

    }

}

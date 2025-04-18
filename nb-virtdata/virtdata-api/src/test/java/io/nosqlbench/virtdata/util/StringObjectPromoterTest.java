/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.util;

import io.nosqlbench.virtdata.core.bindings.StringObjectPromoter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class StringObjectPromoterTest {

    @Test
    public void testExplicitString() {
        Object literalValue = StringObjectPromoter.promote("'astring'");
        assertThat(literalValue).isInstanceOf(String.class);
        assertThat(literalValue).isEqualTo("astring");
    }

    @Test
    public void testLongFallback() {
        Object literalValue = StringObjectPromoter.promote(String.valueOf(Long.MAX_VALUE));
        assertThat(literalValue).isInstanceOf(Long.class);
        assertThat(literalValue).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    public void testBigIntegerFallback() {
        Object o = StringObjectPromoter.promote("9223372036854775808");
        assertThat(o).isInstanceOf(BigInteger.class);
        assertThat(o).isEqualTo(new BigInteger("9223372036854775808"));
    }

    @Test
    public void testFloat() {
        Object o = StringObjectPromoter.promote("02.34");
        assertThat(o).isInstanceOf(Float.class);
        assertThat(o).isEqualTo(Float.valueOf("02.34"));
    }

    @Test
    public void testDoubleFallback() {
        Object o = StringObjectPromoter.promote(String.valueOf(Double.MAX_VALUE));
        assertThat(o).isInstanceOf(Double.class);
        assertThat(o).isEqualTo(Double.valueOf(String.valueOf(Double.MAX_VALUE)));
    }

    @Test
    public void testBigDecimalFallback() {
        Object o = StringObjectPromoter.promote("1"+ Double.MAX_VALUE);
        assertThat(o).isInstanceOf(BigDecimal.class);
        assertThat(o).isEqualTo(new BigDecimal("1"+ Double.MAX_VALUE));
    }

    @Test
    public void testExplicitLong() {
        Object o = StringObjectPromoter.promote("3l");
        assertThat(o).isInstanceOf(Long.class);
        assertThat(o).isEqualTo(3L);
    }

    @Test
    public void testExplicitDouble() {
        Object o = StringObjectPromoter.promote("234.0d");
        assertThat(o).isInstanceOf(Double.class);
        assertThat(o).isEqualTo(Double.valueOf("234.0d"));
    }

    @Test
    public void testExplicitFloat() {
        Object o = StringObjectPromoter.promote("123.4f");
        assertThat(o).isInstanceOf(Float.class);
        assertThat(o).isEqualTo(Float.valueOf("123.4f"));
    }

    @Test
    public void testObscureUnquotedString() {
        Object o = StringObjectPromoter.promote("234cavebabel");
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo("234cavebabel");
    }
}

package io.virtdata.util;

import org.junit.Test;

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
        Object o = StringObjectPromoter.promote("1"+String.valueOf(Double.MAX_VALUE));
        assertThat(o).isInstanceOf(BigDecimal.class);
        assertThat(o).isEqualTo(new BigDecimal("1"+String.valueOf(Double.MAX_VALUE)));
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
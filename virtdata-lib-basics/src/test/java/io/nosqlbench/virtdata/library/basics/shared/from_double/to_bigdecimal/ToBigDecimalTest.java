package io.nosqlbench.virtdata.library.basics.shared.from_double.to_bigdecimal;

import org.assertj.core.data.Offset;
import org.junit.Test;

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

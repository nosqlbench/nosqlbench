package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bigdecimal;

import org.assertj.core.data.Offset;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;

public class ToBigDecimalTest {

    @Test
    public void demonstrateLongToBigDecimal() {
        ToBigDecimal wholeValues = new ToBigDecimal();
        BigDecimal whole12345 = wholeValues.apply(12345L);
        assertThat(whole12345).isEqualTo(new BigDecimal(12345L));

        ToBigDecimal pennies = new ToBigDecimal(2);
        BigDecimal pennies12345 = pennies.apply(12345L);
        assertThat(pennies12345).isEqualTo(BigDecimal.valueOf(12345,2));

        ToBigDecimal custom = new ToBigDecimal("precision=5 roundingMode=CEILING");
        BigDecimal c123456 = custom.apply(123456L);
        assertThat(c123456).isEqualTo(new BigDecimal(123456L,new MathContext("precision=5 roundingMode=CEILING")));
        double v = c123456.doubleValue();
        assertThat(v).isCloseTo(123460d, Offset.offset(0.001d));

    }

}

package io.virtdata.libbasics.shared.conversions.from_double;

import org.junit.Test;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ToStringTest {

    @Test
    public void TestNoFunc() {
        io.virtdata.libbasics.shared.conversions.from_long.ToString f = new io.virtdata.libbasics.shared.conversions.from_long.ToString();
        assertThat(f.apply(1L)).isEqualTo("1");
    }

    @Test
    public void TestDoubleUnaryOp() {
        io.virtdata.libbasics.shared.conversions.from_double.ToString f = new io.virtdata.libbasics.shared.conversions.from_double.ToString(new DoubleUnaryOperator() {
            @Override
            public double applyAsDouble(double operand) {
                return operand;
            }
        });
        assertThat(f.apply(2L)).isEqualTo("2.0");
    }

    @Test
    public void TestDoubleFunction() {
        io.virtdata.libbasics.shared.conversions.from_double.ToString f = new io.virtdata.libbasics.shared.conversions.from_double.ToString(new DoubleFunction<Double>() {

            @Override
            public Double apply(double value) {
                return value;
            }
        });
        assertThat(f.apply(3L)).isEqualTo("3.0");
    }

    @Test
    public void TestFunctionDoubleDouble() {
        io.virtdata.libbasics.shared.conversions.from_double.ToString f = new io.virtdata.libbasics.shared.conversions.from_double.ToString(new Function<Double,Double>() {

            @Override
            public Double apply(Double aDouble) {
                return aDouble;
            }
        });
        assertThat(f.apply(4L)).isEqualTo("4.0");
    }


}
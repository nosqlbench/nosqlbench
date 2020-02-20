package io.virtdata.libbasics.shared.from_double.to_double.to_double;

import io.virtdata.libbasics.shared.from_double.to_double.Clamp;
import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClampTest {

    @Test
    public void testDoubleUnaryClamp() {
        Clamp clamp = new Clamp(90.0d, 103.0d);
        assertThat(clamp.applyAsDouble(9.034D)).isCloseTo(90.0d, Offset.offset(0.0000001D));
        assertThat(clamp.applyAsDouble(90.34D)).isCloseTo(90.34d, Offset.offset(0.0000001D));
        assertThat(clamp.applyAsDouble(903.4D)).isCloseTo(103.0d, Offset.offset(0.0000001D));
    }

    @Test
    public void testIntUnaryClamp() {
        io.virtdata.libbasics.shared.unary_int.Clamp clamp = new io.virtdata.libbasics.shared.unary_int.Clamp(9, 13);
        assertThat(clamp.applyAsInt(8)).isEqualTo(9);
        assertThat(clamp.applyAsInt(9)).isEqualTo(9);
        assertThat(clamp.applyAsInt(10)).isEqualTo(10);
        assertThat(clamp.applyAsInt(100)).isEqualTo(13);
    }

    @Test
    public void testLongUnaryClamp() {
        io.virtdata.libbasics.shared.from_long.to_long.Clamp clamp = new io.virtdata.libbasics.shared.from_long.to_long.Clamp(9, 13);
        assertThat(clamp.applyAsLong(8L)).isEqualTo(9L);
        assertThat(clamp.applyAsLong(9L)).isEqualTo(9L);
        assertThat(clamp.applyAsLong(10L)).isEqualTo(10L);
        assertThat(clamp.applyAsLong(100L)).isEqualTo(13L);
    }

}
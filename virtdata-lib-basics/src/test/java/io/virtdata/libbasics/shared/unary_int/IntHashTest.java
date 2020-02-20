package io.virtdata.libbasics.shared.unary_int;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntHashTest {

    @Test
    public void testIntHash() {
        Hash ih = new Hash();
        assertThat(ih.applyAsInt(1)).isEqualTo(1476114732);
        assertThat(ih.applyAsInt(2)).isEqualTo(1829715121);
        assertThat(ih.applyAsInt(3)).isEqualTo(51951665);
    }

}
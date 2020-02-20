package io.virtdata.long_double;

import io.virtdata.libbasics.shared.from_long.to_double.HashedDoubleRange;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashedDoubleRangeTest {

    @Test
    public void testBasicRanges() {
        HashedDoubleRange r = new HashedDoubleRange(0.0D, 100.0D);
        for(long i=1;i<1000;i++) {
            assertThat(r.applyAsDouble(i)).isBetween(0.0D,100.0D);
        }

    }

}
package io.virtdata.libbasics.shared.from_long.to_long;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashRangeScaledTest {

    @Test
    public void testRanging() {
        HashRangeScaled hrs = new HashRangeScaled();
        for (long i = 0; i < 100; i++) {
            long l = hrs.applyAsLong(i);
            assertThat(l).isBetween(0L,i);
        }
    }

}
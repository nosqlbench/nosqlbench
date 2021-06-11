package io.nosqlbench.virtdata.library.basics.tests.long_long;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashRangeTest {

    @Test
    public void testFixedSize() {
        HashRange hashRange = new HashRange(65);
        assertThat(hashRange.applyAsLong(32L)).isEqualTo(11L);
    }

    @Test
    public void testSingleElementRange() {
        HashRange hashRange = new HashRange(33L,33L);
        long l = hashRange.applyAsLong(93874L);
        assertThat(l).isEqualTo(33L);
    }

}
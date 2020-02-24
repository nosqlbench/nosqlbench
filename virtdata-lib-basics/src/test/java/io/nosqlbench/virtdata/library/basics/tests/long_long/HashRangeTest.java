package io.nosqlbench.virtdata.library.basics.tests.long_long;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashRangeTest {

    @Test
    public void testFixedSize() {
        HashRange hashRange = new HashRange(65);
        assertThat(hashRange.applyAsLong(32L)).isEqualTo(11L);
    }

}
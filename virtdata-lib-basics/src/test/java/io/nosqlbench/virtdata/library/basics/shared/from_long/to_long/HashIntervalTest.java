package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashIntervalTest {

    @Test
    public void testBasicRange() {
        HashInterval hi = new HashInterval(3L, 5L);
        long r1 = hi.applyAsLong(43L);
        assertThat(r1).isEqualTo(4L);

    }

    @Test(expected = BasicError.class)
    public void testRangeError() {
        HashInterval hi = new HashInterval(3L, 3L);
    }
}
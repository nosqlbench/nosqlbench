package io.virtdata.discrete.common;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class ThreadSafeHashTest {

    @Test
    public void testExtremeValues() {
        ThreadSafeHash threadSafeHash = new ThreadSafeHash();
        long part =Long.MAX_VALUE >> 4; // 1/16 of positive long domain
        for (long i = 0; i < 20; i++) {
            //long v = Math.multiplyExact(i,part);
            long v = i*part & Long.MAX_VALUE;
            assertThat(v).isNotNegative();
            assertThat(threadSafeHash.applyAsLong(v)).isNotNegative();
        }
    }
}
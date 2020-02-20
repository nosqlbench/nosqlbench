package io.virtdata.libbasics.shared.stateful;

import io.virtdata.libbasics.core.threadstate.SharedState;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullOrPassTest {

    @Test
    public void testRanging() {
        NullOrPass f = new NullOrPass(.10d, "value");
        SharedState.tl_ObjectMap.get().put("value",12345L);
        Object v = f.apply(2345L);
        assertThat(v).isOfAnyClassIn(Long.class);
        assertThat((Long)v).isEqualTo(2345L);
    }

    @Test
    public void testRatio100pct() {
        NullOrPass f = new NullOrPass(1.0,"value");
        NullOrPass g = new NullOrPass(0.0,"value");
    }

    @Test(expected = RuntimeException.class)
    public void testLowRatio() {
        NullOrPass f = new NullOrPass(-0.00001d,"value");
    }

    @Test(expected = RuntimeException.class)
    public void testHighRatio() {
        NullOrPass g = new NullOrPass(1.000001d,"value");
    }

}
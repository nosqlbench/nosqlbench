package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

    @Test
    public void testLowRatio() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new NullOrPass(-0.00001d,"value"));
    }

    @Test
    public void testHighRatio() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new NullOrPass(1.000001d,"value"));
    }

}
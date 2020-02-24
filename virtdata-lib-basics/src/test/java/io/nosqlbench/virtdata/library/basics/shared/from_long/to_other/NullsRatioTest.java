package io.nosqlbench.virtdata.library.basics.shared.from_long.to_other;

import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import io.nosqlbench.virtdata.library.basics.shared.stateful.NullOrPass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullsRatioTest {

    @Test
    public void testNullsRatio() {
        NullOrPass nrf = new NullOrPass(.4D, "c");
        SharedState.tl_ObjectMap.get().put("c",23L);
        Object result = nrf.apply(39L);
        assertThat(result).isNull();
    }


}
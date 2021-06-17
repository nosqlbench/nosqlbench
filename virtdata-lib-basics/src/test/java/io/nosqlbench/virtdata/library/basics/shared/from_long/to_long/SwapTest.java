package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.library.basics.shared.stateful.Clear;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SwapTest {

    @Test
    public void swapValues() {
        new Clear().apply(0L);
        Swap shazzbot = new Swap("shazzbot", 1001L);
        long l = shazzbot.applyAsLong(234L);
        assertThat(l).isEqualTo(1001L);
        long m = shazzbot.applyAsLong(444L);
        assertThat(m).isEqualTo(234L);
        long justloaded = new Load("shazzbot").applyAsLong(4444444);
        assertThat(justloaded).isEqualTo(444L);

    }

}
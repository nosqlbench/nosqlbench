package io.nosqlbench.virtdata.core;

import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtDataTest {

    @Test
    public void testBasicBindings() {
        BindingsTemplate bt = VirtData.getTemplate("a","Mod(5)");
        assertThat(bt).isNotNull();
    }

}

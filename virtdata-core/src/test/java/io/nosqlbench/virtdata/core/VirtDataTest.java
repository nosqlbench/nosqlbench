package io.nosqlbench.virtdata.core;

import io.nosqlbench.virtdata.api.BindingsTemplate;
import io.nosqlbench.virtdata.api.VirtData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtDataTest {

    @Test
    public void testBasicBindings() {
        BindingsTemplate bt = VirtData.getTemplate("a","Mod(5)");
        assertThat(bt).isNotNull();
    }

}
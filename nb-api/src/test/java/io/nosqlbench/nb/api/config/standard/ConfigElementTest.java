package io.nosqlbench.nb.api.config.standard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigElementTest {

    @Test
    public void testRegex() {
        Param<String> cfgmodel = Param.defaultTo("testvar", "default").setRegex("WOO");
        assertThat(cfgmodel.validate("WOO").isValid()).isTrue();
    }
}

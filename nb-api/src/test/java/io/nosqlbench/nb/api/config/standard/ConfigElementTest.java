package io.nosqlbench.nb.api.config.standard;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigElementTest {

    @Test
    public void testRegex() {
        Param<String> cfgmodel =
            new Param<>("testvar",String.class,"testing a var",false,null).setRegex("WOO");
        assertThat(cfgmodel.validate("WOO").isValid()).isTrue();
    }
}

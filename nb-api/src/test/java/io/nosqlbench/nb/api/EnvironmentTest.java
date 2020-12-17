package io.nosqlbench.nb.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentTest {

    @Test
    public void testInterpolation() {
        Environment env = new Environment();
        String home1 = env.interpolate("home is '$HOME'").orElse(null);
        assertThat(home1).matches(".+");
        String home2 = env.interpolate("home is '${home}'").orElse(null);
        assertThat(home1).matches(".+");
    }

}
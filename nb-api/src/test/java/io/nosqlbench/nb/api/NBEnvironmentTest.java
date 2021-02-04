package io.nosqlbench.nb.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NBEnvironmentTest {

    @Test
    public void testInterpolation() {
        NBEnvironment env = new NBEnvironment();
        String home1 = env.interpolate("home is '$HOME'").orElse(null);
        assertThat(home1).matches(".+");
        String home2 = env.interpolate("home is '${home}'").orElse(null);
        assertThat(home1).matches(".+");
    }

}

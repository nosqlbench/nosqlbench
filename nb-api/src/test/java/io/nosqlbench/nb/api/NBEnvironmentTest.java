package io.nosqlbench.nb.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

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

    @Test
    public void testInterpolationWithTimestamp() {
        NBEnvironment env = new NBEnvironment();
        long millis = 1633964892320L;
        String time1 = env.interpolateWithTimestamp("word WOO %td %% end", millis, Map.of("WOO","WOW")).orElse(null);
        assertThat(time1).isEqualTo("word WOW 11 % end");

    }

}

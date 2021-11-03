package io.nosqlbench.nb.api;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

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
        String time1 = env.interpolateWithTimestamp("word WOO$WOO %td %% end", millis, Map.of("WOO","WOW")).orElse(null);
        assertThat(time1).isEqualTo("word WOOWOW 11 % end");
    }

    @Test
    public void testInterpolationPrecedence() {
        NBEnvironment env = new NBEnvironment();
        Optional<String> superseded = env.interpolate("$TEST_KEY, $USER", Map.of("TEST_KEY", "supersedes1", "USER", "supersedes2"));
        assertThat(superseded).contains("supersedes1, supersedes2");
        superseded = env.interpolate("$USER", Map.of("TEST_KEY", "supersedes1"));
        assertThat(superseded).isPresent();
        assertThat(superseded.get()).isNotEqualTo("supersedes2");
    }

}

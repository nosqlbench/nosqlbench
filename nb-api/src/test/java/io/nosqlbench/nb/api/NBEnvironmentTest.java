package io.nosqlbench.nb.api;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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

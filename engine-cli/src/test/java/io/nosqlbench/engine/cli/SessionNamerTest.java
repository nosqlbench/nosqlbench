/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.cli;

import io.nosqlbench.api.metadata.SessionNamer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionNamerTest {

    @Test
    public void testDefaultFormat() {
        SessionNamer namer = new SessionNamer();
        String name1 = SessionNamer.format(null);
        assertThat(name1).matches("scenario_\\d{8}_\\d{6}_\\d{3}");
        String name2 = SessionNamer.format("");
        assertThat(name2).matches("scenario_\\d{8}_\\d{6}_\\d{3}");
    }

    @Test
    public void testCustomFormat() {
        SessionNamer namer = new SessionNamer();
        String name1 = SessionNamer.format("Custom_session_name");
        assertThat(name1).matches("Custom_session_name");
        String name2 = SessionNamer.format("TEST--%tQ");
        assertThat(name2).matches("TEST--\\d{13}");
    }


}

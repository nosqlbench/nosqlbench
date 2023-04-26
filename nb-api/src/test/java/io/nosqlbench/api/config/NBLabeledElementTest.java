/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.config;

import io.nosqlbench.api.config.NBLabeledElement.MapLabels;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NBLabeledElementTest {

    @Test
    public void testBasicNameScenario() {
        final MapLabels e1 = NBLabeledElement.forMap(Map.of());
        assertThat(e1.linearizedByValueGraphite("name","testname")).isEqualTo("testname");
    }

}

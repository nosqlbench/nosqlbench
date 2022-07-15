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

package io.nosqlbench.nb.api;

import io.nosqlbench.api.metadata.SystemId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemIdTest {

    @Test
    public void testHostInfo() {
        String info = SystemId.getHostSummary();
        System.out.println(info);
    }

    @Test
    public void testNostId() {
        String info = SystemId.getNodeId();
        assertThat(info).matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    @Test
    public void testNodeFingerprint() {
        String hash = SystemId.getNodeFingerprint();
        assertThat(hash).matches("[A-Z0-9]+");
    }

}

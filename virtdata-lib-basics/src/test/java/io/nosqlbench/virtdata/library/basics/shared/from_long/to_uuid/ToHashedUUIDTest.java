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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_uuid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ToHashedUUIDTest {

    @Test
    public void testHashedUUID() {
        ToHashedUUID thu = new ToHashedUUID();
        UUID uuid = thu.apply(1L);
        assertThat(uuid.variant()).isEqualTo(2);
        assertThat(uuid.version()).isEqualTo(4);
        assertThat(uuid.toString()).isEqualTo("5752fae6-9d16-43da-b20f-557a1dd5c571");
    }

}

/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapters.api.activityconfig;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiMapLookupTest {

    @Test
    public void testGet() throws Exception {
        Map<String,String> m1 = new HashMap<>();
        Map<String,String> m2 = new HashMap<>();
        m1.put("a", String.valueOf(1L));
        m2.put("b", "c");
        MultiMapLookup mml = new MultiMapLookup(m1, m2);
        assertThat(mml.get("a")).isEqualTo("1");
    }
}

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

package io.nosqlbench.api.content;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class NBIOSetsTest {

    @Test
    public void testSetsAddition() {
        List<Set<String>> data = new ArrayList<>();
        data=NBIOSets.combine(data, Set.of("a","b"));
        assertThat(data).isEqualTo(List.of(Set.of("a","b")));
        data=NBIOSets.combine(data,Set.of("a","c"));
        assertThat(data).isEqualTo(List.of(Set.of("a","b","c")));
        data=NBIOSets.combine(data, Set.of("d"));
        assertThat(data).isEqualTo(List.of(Set.of("a","b","c"),Set.of("d")));
    }

}

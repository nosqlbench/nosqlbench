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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class PartitionLongsTest {

    @Test
    void testInvalidInitializerValues() {
        Assertions.assertThrows(RuntimeException.class, () -> new PartitionLongs(-3));
        Assertions.assertThrows(RuntimeException.class, () -> new PartitionLongs(0));
    }

    @Test
    public void testValueCardinality() {
        PartitionLongs f = new PartitionLongs(15);
        Set<Long> values = new HashSet<Long>();
        for (int i = 0; i < 100; i++) {
            values.add(f.applyAsLong(i));
        }
        Assertions.assertEquals(15, values.size());
    }
}

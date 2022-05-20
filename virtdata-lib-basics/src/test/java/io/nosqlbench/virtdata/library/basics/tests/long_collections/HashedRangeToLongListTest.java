package io.nosqlbench.virtdata.library.basics.tests.long_collections;

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


import io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.HashedRangeToLongList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HashedRangeToLongListTest {

    @Test
    public void longListRangeTest() {
        HashedRangeToLongList gener = new HashedRangeToLongList(3, 6, 9, 12);
        for (int i = 0; i < 100; i++) {
            System.out.println("long list range test size: " + i);
            List<Long> list= gener.apply(i);
            assertThat(list.size()).isBetween(9,12);
            for (Long longVal : list) {
                assertThat(longVal.longValue()).isBetween(3L,6L);
            }
        }
    }

}

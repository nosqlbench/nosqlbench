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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashRange;
import org.junit.jupiter.api.Test;

import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ListTest {

    @Test
    public void testList() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.List lf =
                new io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.List(new HashRange(2, 3), (LongFunction) (l) -> "_" + l);
        java.util.List<Object> l1 = lf.apply(2L);
        assertThat(l1).containsExactly("_2","_3","_4");
    }

    @Test
    public void testStringList() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.StringList slf = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.StringList((s) -> 2,(v)->v);
        java.util.List<String> sl1 = slf.apply(13L);
        assertThat(sl1).containsExactly("13","14");
    }
}

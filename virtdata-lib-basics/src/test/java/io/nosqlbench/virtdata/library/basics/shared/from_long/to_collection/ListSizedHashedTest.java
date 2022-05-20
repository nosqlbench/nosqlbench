package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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



import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.NumberNameToString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSizedHashedTest {

    @Test
    public void testTwoPartExample() {
        LongToIntFunction sizer = (l) -> (int) l;
        LongFunction<String> namer = (l) -> "L{" + l + "}";

        ListSizedHashed f1 = new ListSizedHashed(sizer, namer);
        List<Object> for37 = f1.apply(37L);
        assertThat(for37).hasSize(37);
        assertThat(for37.get(0)).isNotEqualTo(for37.get(36));
        for (Object o : for37) {
            System.out.println(o);
        }

    }

    @Test
    public void testFunctionSelection() {
        LongToIntFunction sizer = (l) -> (int) l;
        LongFunction<String> namer = (l) -> "L{" + l + "}";
        LongFunction<String> brackets = (l) -> "[[" + l + "]]";

        ListSizedHashed f2 = new ListSizedHashed(sizer, namer, namer, brackets, namer);
        List<Object> for53 = f2.apply(53L);
        assertThat(for53).hasSize(53);
        assertThat(for53.get(2).toString()).startsWith("[[");
        assertThat(for53.get(52)).isOfAnyClassIn(String.class);

    }

}

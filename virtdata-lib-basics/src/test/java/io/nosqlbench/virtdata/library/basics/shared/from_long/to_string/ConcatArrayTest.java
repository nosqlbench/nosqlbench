/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.library.basics.shared.conversions.from_any.ToJSONFPretty;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.ListSized;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.ListSizedStepped;
import org.junit.jupiter.api.Test;

import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ConcatArrayTest {
    @Test
    public void testConcatArray() {
        ConcatArray ca = new ConcatArray(",", 2, "{\n{}\n}", new NumberNameToString());
        assertThat(ca.apply(3L)).isEqualTo("""
            {
            three,four
            }""");
    }

    @Test
    public void testConcatArrayJson() {
        ToJSONFPretty jsonlist = new ToJSONFPretty(
            (LongFunction) new ListSizedStepped(4,
                new NumberNameToString()));

        ConcatArray ca = new ConcatArray(
            ",", 2, "{\n{}\n}", jsonlist);

        assertThat(ca.apply(3L)).isEqualTo("""
            {
            [
              "three",
              "four",
              "five",
              "six"
            ],[
              "four",
              "five",
              "six",
              "seven"
            ]
            }""");
    }

}

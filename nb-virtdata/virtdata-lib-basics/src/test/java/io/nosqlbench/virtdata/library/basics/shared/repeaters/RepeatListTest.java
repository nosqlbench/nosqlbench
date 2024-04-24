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

package io.nosqlbench.virtdata.library.basics.shared.repeaters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RepeatListTest {

    @Test
    public void testRepeatList() {
        List<Double> doubles = List.of(1.2, 3.4, 5.6);
        RepeatList repeater = new RepeatList(7);
        List repeated = repeater.apply(doubles);
        assertThat(repeated).containsExactly(1.2, 3.4, 5.6, 1.2, 3.4, 5.6, 1.2);
    }

}

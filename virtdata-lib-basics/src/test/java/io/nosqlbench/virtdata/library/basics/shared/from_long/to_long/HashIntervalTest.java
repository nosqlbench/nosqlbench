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

import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class HashIntervalTest {

    @Test
    public void testBasicRange() {
        HashInterval hi = new HashInterval(3L, 5L);
        long r1 = hi.applyAsLong(43L);
        assertThat(r1).isEqualTo(4L);

    }

    @Test
    public void testRangeError() {
        assertThatExceptionOfType(BasicError.class)
                .isThrownBy(() -> new HashInterval(3L, 3L));
    }
}

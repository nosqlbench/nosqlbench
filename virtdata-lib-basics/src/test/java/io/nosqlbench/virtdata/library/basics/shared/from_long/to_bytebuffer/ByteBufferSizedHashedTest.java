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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashRange;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferSizedHashedTest {

    @Test
    public void testWithHashRange() {
        LongToIntFunction sizeFunc = new HashRange(100, 1000);
        long input = 233423L;

        ByteBufferSizedHashed d1 = new ByteBufferSizedHashed(sizeFunc);
        ByteBuffer buf = d1.apply(input);
        assertThat(sizeFunc.applyAsInt(233423L)).isEqualTo(buf.remaining());
    }

}

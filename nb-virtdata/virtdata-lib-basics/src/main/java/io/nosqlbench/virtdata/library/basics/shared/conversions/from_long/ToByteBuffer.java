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

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;

/**
 * Convert the input value to a {@code ByteBuffer}
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByteBuffer implements LongFunction<ByteBuffer> {

    private final int allocSize;
    private final int bufSize;

    public ToByteBuffer() {
        this.allocSize = Long.BYTES;
        this.bufSize = Long.BYTES;
    }

    @Example({"ToByteBuffer(13)", "Repeat the input long value to make a 13byte buffer"})
    public ToByteBuffer(int size) {
        this.bufSize = size;
        this.allocSize = ((size + Long.BYTES - 1) / Long.BYTES) * Long.BYTES;
    }

    @Override
    public ByteBuffer apply(long input) {
        ByteBuffer buffer = ByteBuffer.allocate(allocSize);
        while (buffer.remaining() >= Long.BYTES) {
            buffer.putLong(input);
        }
        buffer.position(this.bufSize);
        buffer.flip();
        return buffer;
    }

}

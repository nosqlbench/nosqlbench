package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

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


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a ByteBuffer from a long input based on a provided size function.
 *
 * As a 'Sized' function, the first argument is a function which determines the size of the resulting ByteBuffer.
 *
 * As a 'Hashed' function, the input value is hashed again before being used as value.
 */
@Categories({Category.conversion,Category.general})
@ThreadSafeMapper
public class ByteBufferSizedHashed implements LongFunction<ByteBuffer> {

    private final LongToIntFunction sizeFunc;
    private final Hash hash = new Hash();


    @Example({
        "ByteBufferSizedHashed(16)",
        "Functionally identical to HashedtoByteBuffer(16) but using dynamic sizing implementation"
    })
    @Example({
        "ByteBufferSizedHashed(HashRange(10, 14))",
        "Create a ByteBuffer with variable limit (10 to 14)"
    })
    public ByteBufferSizedHashed(int size) {
        this.sizeFunc = s -> size;
    }

    public ByteBufferSizedHashed(Object sizeFunc) {
        if (sizeFunc instanceof Number) {
            int size = ((Number) sizeFunc).intValue();
            this.sizeFunc = s -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
    }

    @Override
    public ByteBuffer apply(long input) {
        int length = sizeFunc.applyAsInt(input);

        int longs = (length / Long.BYTES) + 1;
        int bytes = longs * Long.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        for (int i = 0; i < longs; i++) {
            long l = hash.applyAsLong(input + i);
            buffer.putLong(l);
        }
        buffer.flip();
        buffer.limit(length);
        return buffer;
    }
}

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_charbuffer;

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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Hash;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.CharBufImage;

import java.nio.CharBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a CharBuffer from the first function, and then sample data from
 * that buffer according to the size function. The initFunction can be
 * given as simply a size, in which case ByteBufferSizedHash is used with Hex String
 * conversion.
 * If the size function yields a size larger than the available buffer size, then it is
 * lowered to that size automatically. If it is lower, then a random offset
 * is used within the buffer image.
 *
 * This function behaves slightly differently than most in that it creates and
 * caches as source byte buffer during initialization.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class CharBufferExtract implements LongFunction<CharBuffer> {

    private final CharBuffer image;
    private final LongToIntFunction sizefunc;
    private final Hash posFunc = new Hash();
    private final int imgsize;

    public CharBufferExtract(Object initFunc, Object sizeFunc) {
        CharBuffer image = null;
        if (initFunc instanceof Number) {
            int bufsize = ((Number) initFunc).intValue();
            this.image = new CharBufImage(bufsize).apply(1L);
        } else {
            LongFunction<String> bbfunc = VirtDataConversions.adaptFunction(initFunc, LongFunction.class, String.class);
            this.image = CharBuffer.wrap(bbfunc.apply(0));
        }
        this.imgsize = this.image.limit();

        if (sizeFunc instanceof Number) {
            int size = ((Number) sizeFunc).intValue();
            this.sizefunc = l -> size;
        } else {
            this.sizefunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
    }

    @Override
    public CharBuffer apply(long value) {
        int size = Math.min(sizefunc.applyAsInt(value), imgsize);
        int pos = posFunc.applyAsInt(value);
        pos = pos % ((imgsize - size) + 1);
        return image.subSequence(pos, pos + size);
    }
}

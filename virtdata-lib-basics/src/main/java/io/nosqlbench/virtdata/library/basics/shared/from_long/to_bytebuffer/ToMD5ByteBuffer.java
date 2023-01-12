/*
 * Copyright (c) 2022-2023 nosqlbench
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

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;

/**
 * Converts the byte image of the input long to a MD5 digest in ByteBuffer form.
 * Deprecated usage due to unsafe MD5 digest.
 * Replaced with DigestToByteBuffer with MD5 when absolutely needed for existing NB tests.
 * However, stronger encryption algorithms (e.g. SHA-256) are recommended due to MD5's limitations.
 */
@Categories({Category.conversion, Category.premade})
@ThreadSafeMapper
@Deprecated(since = "NB5", forRemoval = true)
public class ToMD5ByteBuffer implements LongFunction<ByteBuffer> {


    /**
     * Deprecated usage due to unsafe MD5 digest.
     * Use the DigestToByteBuffer with alternatives other than MD5.
     */
    @Example({"MD5ByteBuffer()", "convert the a input to an md5 digest of its bytes"})
    @Deprecated
    public ToMD5ByteBuffer() {
        throw new RuntimeException("No longer available.  Please use the DigestToByteBuffer with " +
                "alternatives other than MD5");
    }

    /**
     * Deprecated usage due to unsafe MD5 digest used in this class.
     * Use the DigestToByteBuffer with alternatives other than MD5.
     */
    @Override
    @Deprecated
    public ByteBuffer apply(long value) {
        throw new RuntimeException("No longer available.  Please use the DigestToByteBuffer with " +
                "alternatives other than MD5");
    }


}

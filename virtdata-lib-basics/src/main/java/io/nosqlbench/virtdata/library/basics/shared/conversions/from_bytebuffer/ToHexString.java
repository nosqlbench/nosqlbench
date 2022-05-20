package io.nosqlbench.virtdata.library.basics.shared.conversions.from_bytebuffer;

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
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Converts the input ByteBuffer to a hexadecimal String.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToHexString implements Function<ByteBuffer,String> {

    private final boolean useUpperCase;

    /**
     * Convert the ByteBuffer's contents to a hex string using upper case by default.
     */
    public ToHexString() {
        this(true);
    }

    /**
     * Convert the ByteBuffer's contents to a hex string upper or lower case.
     */
    public ToHexString(boolean useUpperCase) {
        this.useUpperCase = useUpperCase;
    }

    @Override
    public String apply(ByteBuffer byteBuffer) {
        return Hex.encodeHexString(byteBuffer,useUpperCase);
    }
}

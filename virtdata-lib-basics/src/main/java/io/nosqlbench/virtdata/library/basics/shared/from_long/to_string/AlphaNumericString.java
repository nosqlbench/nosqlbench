package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

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

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create an alpha-numeric string of the specified length, character-by-character.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class AlphaNumericString implements LongFunction<String> {
    private static final String AVAILABLE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final ThreadLocal<StringBuilder> threadStringBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final Hash hash = new Hash();
    private final LongToIntFunction lengthFunc;


    @Example({
        "AlphaNumericString(16)",
        "Create an alpha numeric String with length of 16"
    })
    @Example({
        "AlphaNumericString(HashRange(10, 14))",
        "Create an alpha numeric String with length variable limit (10 to 14)"
    })
    public AlphaNumericString(int length) {
        this.lengthFunc = s -> length;
    }

    public AlphaNumericString(Object lengthFunc) {
        if (lengthFunc instanceof Number) {
            int size = ((Number) lengthFunc).intValue();
            this.lengthFunc = s -> size;
        } else {
            this.lengthFunc = VirtDataConversions.adaptFunction(lengthFunc, LongToIntFunction.class);
        }
    }

    @Override
    public String apply(long operand)
    {
        int length = lengthFunc.applyAsInt(operand);

        long hashValue = operand;
        StringBuilder sb = threadStringBuilder.get();
        sb.setLength(0);
        for (int i = 0; i < length; i++)
        {
            hashValue = hash.applyAsLong(hashValue);
            int randomPos = (int) (hashValue % AVAILABLE_CHARS.length());
            sb.append(AVAILABLE_CHARS.charAt(randomPos));
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "AlphaNumericString(length=" + lengthFunc.applyAsInt(0) + ")";
    }
}

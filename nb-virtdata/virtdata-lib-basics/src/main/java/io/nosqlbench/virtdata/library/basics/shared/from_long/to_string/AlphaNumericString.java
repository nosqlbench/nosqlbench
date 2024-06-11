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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

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
//    private final ThreadLocal<StringBuilder> threadStringBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final Hash hash = new Hash();
    private final LongToIntFunction lengthFunc;

    @Example({
        "AlphaNumericString(10)",
        "Create a 10-character alpha-numeric string"
    })
    @Example({
        "AlphaNumericString(HashRange(10, 20))",
        "Create an alpha-numeric string with a length between 10 and 20 characters"
    })
    public AlphaNumericString(int length) {
        this.lengthFunc = l -> length;
    }

    public AlphaNumericString(Object lengthfunc) {
        if (lengthfunc instanceof Number) {
            int length = ((Number) lengthfunc).intValue();
            this.lengthFunc = l -> length;
        }
        else {
            this.lengthFunc = VirtDataConversions.adaptFunction(lengthfunc, LongToIntFunction.class);
        }
    }

    @Override
    public String apply(long operand)
    {
        int length = lengthFunc.applyAsInt(operand);
        if (length < 0)
        {
            throw new RuntimeException("AlphaNumericString must have length >= 0");
        }

        long hashValue = operand;
        StringBuilder sb = new StringBuilder();
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
        return "AlphaNumericString(lengthFunc.class=" + lengthFunc.getClass().getSimpleName() + ")";
    }
}

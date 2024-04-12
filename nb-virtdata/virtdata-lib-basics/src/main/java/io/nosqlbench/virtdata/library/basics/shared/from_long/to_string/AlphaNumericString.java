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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongFunction;

/**
 * Create an alpha-numeric string of the specified length, character-by-character.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class AlphaNumericString implements LongFunction<String> {
    private static final String AVAILABLE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final ThreadLocal<StringBuilder> threadStringBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final Hash hash = new Hash();
    private final int length;

    public AlphaNumericString(int length)
    {
        if (length < 0)
        {
            throw new RuntimeException("AlphaNumericString must have length >= 0");
        }
        this.length = length;
    }

    @Override
    public String apply(long operand)
    {
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
        return "AlphaNumericString(length=" + length + ")";
    }
}

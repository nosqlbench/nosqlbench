/*
 * Copyright (c) 2024 nosqlbench
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
 * <P>This is a variant of Concat which chains the hash values
 * from step to step so that each of the provided functions will
 * yield unrelated values. The first input value to a function
 * is a hash of the cycle input value, the next is a hash of the
 * first input value, and so on.</P>
 */
@ThreadSafeMapper
@Categories(Category.general)
public class ConcatChained extends Concat {
    private final static Hash hash = new Hash();
    public ConcatChained(String template, Object... functions) {
        super((c,s) -> hash.applyAsLong(c+s), template, functions);
    }

    @Override
    public String apply(long cycle) {
        StringBuilder buffer = new StringBuilder();
        buffer.setLength(0);
        buffer.append(literals[0]);
        long value = cycle;
        for (int i = 0; i < literals.length-1; i++) {
            value = hash.applyAsLong(value);
            buffer.append(literals[i]);
            int funcIdx = Math.min(functions.length - 1, i);
            LongFunction<String> selectedFunction = functions[funcIdx];
            String string = selectedFunction.apply(value);
            buffer.append(string);
        }
        buffer.append(literals[literals.length-1]);
        return buffer.toString();
    }


}

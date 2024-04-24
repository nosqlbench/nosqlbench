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
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * This variant of Concat allows you to apply a string concatenation to a series of
 * string produced by the provided functions. Each position of a delimiter will simply contain
 * all generated values, although usually, you won't need more than one.
 */
@ThreadSafeMapper
@Categories({Category.functional})
public class ConcatArray extends Concat {
    private final int size;
    private final String delimiter;

    @Example({"ConcatArray(',',5,'{{}}', NumberNameToString())"})
    public ConcatArray(String delimiter, int size, String template, Object... functions) {
        super(template, functions);
        this.delimiter = delimiter;
        this.size = size;
    }

    @Override
    public String apply(long cycle) {
        StringBuilder buffer = new StringBuilder(1024);
        for (int i = 0; i < literals.length - 1; i++) {
            buffer.append(literals[i]);
            for (int element = 0; element < this.size; element++) {
                long value = cycleStepMapper.applyAsLong(cycle, element);
                int funcIdx = Math.min(functions.length - 1, element);
                LongFunction<String> selectedFunction = functions[funcIdx];
                String string = selectedFunction.apply(value);
                buffer.append(string).append(delimiter);
            }
            buffer.setLength(buffer.length()-delimiter.length());
        }
        buffer.append(literals[literals.length - 1]);
        return buffer.toString();
    }

}

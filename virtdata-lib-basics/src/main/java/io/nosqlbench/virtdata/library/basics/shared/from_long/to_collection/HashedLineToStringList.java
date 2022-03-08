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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedLineToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Creates a List&lt;String&gt; from a list of words in a file.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedLineToStringList implements LongFunction<List> {

    private final HashedLineToString hashedLineToString;
    private final HashRange hashRange;

    public HashedLineToStringList(String filename, int minSize, int maxSize) {
        this.hashedLineToString = new HashedLineToString(filename);
        this.hashRange = new HashRange(minSize,maxSize);
    }

    @Override
    public List apply(long value) {
        long size = hashRange.applyAsLong(value);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(hashedLineToString.apply(value+i));
        }
        return list;
    }
}

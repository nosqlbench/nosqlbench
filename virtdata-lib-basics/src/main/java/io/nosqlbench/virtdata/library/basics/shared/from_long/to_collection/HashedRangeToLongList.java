package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Create a list of longs.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedRangeToLongList implements LongFunction<List<Long>> {

    private final HashRange valueRange;
    private final HashRange sizeRange;

    public HashedRangeToLongList(int minVal, int maxVal, int minSize, int maxSize) {
        if (minSize>=maxSize || minSize>=maxSize) {
            throw new RuntimeException("HashedRangeToLongList must have minval, maxval, minsize, maxsize, where min<max.");
        }
        this.valueRange = new HashRange(minVal,maxVal);
        this.sizeRange = new HashRange(minSize,maxSize);
    }

    @Override
    public List<Long> apply(long value) {
        long listSize = sizeRange.applyAsLong(value);
        ArrayList<Long> longList = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            longList.add(valueRange.applyAsLong(value+i));
        }
        return longList;
    }
}

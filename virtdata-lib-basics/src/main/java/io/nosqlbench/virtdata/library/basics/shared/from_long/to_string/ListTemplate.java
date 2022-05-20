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

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code List<String>} based on two functions, the first to
 * determine the list size, and the second to populate the list with
 * string values. The input fed to the second function is incremented
 * between elements.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ListTemplate implements LongFunction<List<String>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<String> valueFunc;

    @Example({"ListTemplate(HashRange(3,7),NumberNameToString())", "create a list between 3 and 7 elements, with number names as the values"})
    public ListTemplate(LongToIntFunction sizeFunc,
                        LongFunction<String> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public List<String> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(valueFunc.apply(value+i));
        }
        return list;
    }
}

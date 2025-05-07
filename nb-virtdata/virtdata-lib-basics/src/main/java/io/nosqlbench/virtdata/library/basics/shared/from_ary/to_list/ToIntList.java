package io.nosqlbench.virtdata.library.basics.shared.from_ary.to_list;

/*
 * Copyright (c) nosqlbench
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Convert an incoming int array to a List of Integers
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToIntList implements Function<int[], List<Integer>> {
    @Override
    public List<Integer> apply(int[] ints) {
        ArrayList<Integer> list = new ArrayList<>(ints.length);
        for (int i : ints) {
            list.add(i);
        }
        return list;
    }
}

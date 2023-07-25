/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.repeaters;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Repeat the incoming list into a new list, filling it to the given size.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class RepeatList implements Function<List, List> {

    private final int size;

    /**
     * Create a list repeater to build up a list from a smaller list.
     * @param size - the total size of the new list
     */
    @Example({"RepeatList(50)","repeat the incoming values into a new List of size 50"})
    public RepeatList(int size) {
        this.size = size;
    }

    @Override
    public List apply(List input) {
        Object[] values = input.toArray();

        ArrayList list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(values[i%values.length]);
        }
        return list;}
}

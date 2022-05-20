package io.nosqlbench.activitytype.cql.datamappers.functions.collections;

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
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * This is an example of a mapping function that can create a list of objects
 * from another internal mapping function.
 *
 * The input value for each function is incremented by one from the initial input value
 * this this overall function.
 *
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class ListMapper implements LongFunction<List<?>> {

    private final int size;
    private final DataMapper<String> elementMapper;

    @Example({"ListMapper(5,NumberNameToString())","creates a list of number names"})
    public ListMapper(int size, String genSpec) {
        this.size = size;
        elementMapper = VirtData.getMapper(genSpec,String.class);
    }

    @Override
    public List<?> apply(long value) {
        List<Object> list = new ArrayList<>(size);
        for (int listpos = 0; listpos < size; listpos++) {
            Object o = elementMapper.get(value + listpos);
            list.add(o);
        }
        return list;
    }
}

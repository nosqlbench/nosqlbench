package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

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

import java.util.function.LongToDoubleFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class FixedValues implements LongToDoubleFunction {

    private final long[] fixedValues;

    @Example({"FixedValues(3D,53D,73d)", "Yield 3D, 53D, 73D, 3D, 53D, 73D, 3D, ..."})
    public FixedValues(long... values) {
        this.fixedValues = values;
    }

    @Override
    public double applyAsDouble(long value) {
        int index = (int) (value % Integer.MAX_VALUE) % fixedValues.length;
        return fixedValues[index];
    }
}

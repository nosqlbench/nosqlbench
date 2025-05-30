package io.nosqlbench.virtdata.library.basics.shared.conversions.from_string;

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

import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.conversion)
public class FromCSVToBoxedFloatArray implements Function<String,Float[]> {

    @Override
    public Float[] apply(String s) {
        String[] split = s.split(",");
        Float[] floats = new Float[split.length];
        for (int i = 0; i < split.length; i++) {
            floats[i] = Float.parseFloat(split[i]);
        }
        return floats;
    }
}

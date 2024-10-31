/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector in List<Number> form, calling the appropriate conversion function
 * depending on the component (Class) type of the incoming List values.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeListVector implements Function<List,List> {
    private final NormalizeDoubleListVector ndv = new NormalizeDoubleListVector();
    private final NormalizeFloatListVector nfv = new NormalizeFloatListVector();

    @Override
    public List apply(List list) {
        if (list.size()==0) {
            return List.of();
        } else if (list.get(0) instanceof Float) {
            return nfv.apply(list);
        } else if (list.get(0) instanceof Double) {
            return ndv.apply(list);
        } else {
            throw new RuntimeException("Only Doubles and Floats are recognized.");
        }
    }
}

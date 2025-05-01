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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.experimental)
public class IntArrayToString implements Function<int[],String> {
    private final ThreadLocal<StringBuilder> threadSb = ThreadLocal.withInitial(StringBuilder::new);

    @Override
    public String apply(int[] ints) {
        StringBuilder sb = threadSb.get();
        sb.setLength(0);
        for (int i : ints) {
            sb.append(i);
            if (i != ints[ints.length - 1]) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}

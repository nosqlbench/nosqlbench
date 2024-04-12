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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Collection;
import java.util.function.Function;

/**
 * This takes any collection and concatenates the String representation
 * with a specified delimiter.
 */
@Categories({Category.general,Category.collections})
@ThreadSafeMapper
public class Join<T> implements Function<Collection<T>,String> {

    private final ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(() -> new StringBuilder());
    private final String delim;
    private final int delimBackstep;

    @Example({"Join(',')","Concatenate the incoming collection with ','"})
    public Join(String delim) {
        this.delim = delim;
        this.delimBackstep = delim.length();
    }

    @Override
    public String apply(Collection<T> collection) {
        StringBuilder sb = tlsb.get();
        sb.setLength(0);
        for (T value : collection) {
            sb.append(value.toString());
            sb.append(delim);
        }
        sb.setLength(sb.length()-delimBackstep);
        return sb.toString();
    }
}

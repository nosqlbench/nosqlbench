package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

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
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.Function;

/**
 * Yields UNSET.value if the input String is not equal to the
 * specified String value. Throws an error if the input value
 * is null. Otherwise, passes the original value along.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class UnsetIfStringNe implements Function<String,Object> {

    private String compareto;

    public UnsetIfStringNe(String compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(String s) {
        if (s!=null && (!s.equals(compareto))) {
            return VALUE.unset;
        }
        if (s!=null) {
            return s;
        }
        throw new RuntimeException("This function is not able to take null values as input. If you need to do that, consider using NullIfNullOrEmpty()");
    }
}

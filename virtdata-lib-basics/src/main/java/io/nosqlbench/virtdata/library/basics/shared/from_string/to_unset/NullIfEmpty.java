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

import java.util.function.Function;

/**
 * Yields a null if the input String is empty. Throws an error if the input
 * String is null.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class NullIfEmpty implements Function<String,String> {

    @Override
    public String apply(String s) {
        if (s!=null && s.isEmpty()) {
            return null;
        }
        if (s!=null) {
            return s;
        }
        throw new RuntimeException("This function is not able to take null values as input. If you need to do that, consider using NullIfNullOrEmpty()");
    }
}

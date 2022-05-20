package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

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

import java.util.function.LongFunction;

/**
 * Always yields the VALUE.unset value, which signals to
 * any consumers that the value provided should be considered
 * undefined for any operation. This is distinct from functions
 * which return a null, which is considered an actual value to
 * be acted upon.
 *
 * It is deemed an error for any downstream user of this library
 * to do anything with VALUE.unset besides explicitly acting like
 * it wasn't provided. That is the point of VALUE.unset.
 *
 * The purpose of having such a value in this library is to provide
 * a value type to help bridge between functional flows and imperative
 * run-times. Without such a value, it would be difficult to simulate
 * value streams in which some of the time values are set and other
 * times they are not.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class Unset implements LongFunction<Object> {
    @Override
    public Object apply(long value) {
        return VALUE.unset;
    }
}

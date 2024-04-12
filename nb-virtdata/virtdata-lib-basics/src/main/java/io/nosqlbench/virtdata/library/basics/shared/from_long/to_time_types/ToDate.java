/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Date;
import java.util.function.LongFunction;

/**
 * Convert the input value to a {@code Date}, by multiplying and then dividing
 * the input by the provided parameters.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class ToDate implements LongFunction<Date> {

    private final long spacing;
    private final long repeat_count;

    @Example({"ToDate(86400000,2)","produce two Date values per day"})
    public ToDate(int millis_multiplier, int millis_divisor){
        this.spacing = millis_multiplier;
        this.repeat_count = millis_divisor;
    }

    @Example({"ToDate(86400000)","produce a single Date() per day"})
    public ToDate(int spacing){
        this(spacing, 1);
    }

    public ToDate() {
        this.spacing=1;
        this.repeat_count=1;
    }

    @Override
    public Date apply(long input) {
        input = (input*spacing)/repeat_count;
        return new Date(input);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + spacing+ ":" + repeat_count;
    }
}

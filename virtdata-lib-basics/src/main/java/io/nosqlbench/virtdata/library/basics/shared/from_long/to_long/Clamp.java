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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Clamp the output values to be at least the minimum value and
 * at most the maximum value.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class Clamp implements LongUnaryOperator {

    private final long min;
    private final long max;

    @Example({"Clamp(4L,400L)","clamp the output values in the range [4L,400L], inclusive"})
    public Clamp(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public long applyAsLong(long operand) {
        return Long.min(max,Long.max(min,operand));
    }
}

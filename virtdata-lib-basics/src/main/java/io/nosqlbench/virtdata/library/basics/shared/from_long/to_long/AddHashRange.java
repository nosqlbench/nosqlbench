/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
@Categories({Category.general})
public class AddHashRange implements LongUnaryOperator {

    private final HashRange hashRange;

    public AddHashRange(long maxValue) {
        this(0, maxValue);
    }

    public AddHashRange(long minValue, long maxValue) {
        if (minValue>maxValue) {
            throw new RuntimeException("minValue must be less than or equal to maxValue");
        }
        this.hashRange = new HashRange(minValue, maxValue);
    }

    @Override
    public long applyAsLong(long operand) {
        return operand + hashRange.applyAsLong(operand);
    }
}

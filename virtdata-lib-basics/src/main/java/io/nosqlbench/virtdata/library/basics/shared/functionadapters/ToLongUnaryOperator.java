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

package io.nosqlbench.virtdata.library.basics.shared.functionadapters;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Adapts any compatible {@link FunctionalInterface} type to a LongUnaryOperator,
 * for use with higher-order functions, when they require a
 * LongUnaryOperator as an argument. Some of the higher-order functions within
 * this library specifically require a LongUnaryOperator as an argument, while
 * some of the other functions are provided in semantically equivalent
 * forms with compatible types which can't be converted directly or
 * automatically by Java.
 *
 * In such cases, those types of functions can be wrapped with the forms
 * described here in order to allow the inner and outer functions to work together.
 */
@ThreadSafeMapper
@Categories({Category.diagnostics})
public class ToLongUnaryOperator implements LongUnaryOperator {

    private final LongUnaryOperator operator;

    public ToLongUnaryOperator(LongFunction<Long> f) {
        this.operator = f::apply;
    }

    public ToLongUnaryOperator(Function<Long,Long> f) {
        this.operator = f::apply;
    }

    public ToLongUnaryOperator(LongUnaryOperator f) {
        this.operator = f;
    }

    @Override
    public long applyAsLong(long operand) {
        return operator.applyAsLong(operand);
    }
}

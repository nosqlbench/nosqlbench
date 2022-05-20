package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

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
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_byte.LongToByte;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_short.LongToShort;

import java.util.function.*;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToString implements LongFunction<String> {
    private final LongFunction<Object> func;

    public ToString() {
        func=(i) -> i;
    }

    public ToString(LongUnaryOperator f) {
        func = f::applyAsLong;
    }

    public ToString(LongFunction<?> f) {
        func = f::apply;
    }

    public ToString(Function<Long,?> f) {
        func = f::apply;
    }

    public ToString(LongToIntFunction f) {
        func = f::applyAsInt;
    }

    public ToString(LongToDoubleFunction f) {
        func = f::applyAsDouble;
    }

    public ToString(LongToByte f) {
        func = f::apply;
    }

    public ToString(LongToShort f) {
        func = f::apply;
    }

    public String apply(long l) {
        Object o = func.apply(l);
        return String.valueOf(o);
    }
}

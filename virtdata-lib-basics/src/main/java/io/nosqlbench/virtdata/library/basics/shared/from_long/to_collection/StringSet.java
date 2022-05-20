package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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


import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.HashSet;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a {@code Set<String>} from a long
 * based on two functions, the first to
 * determine the set size, and the second to populate the set with
 * String values. The input fed to the second function is incremented
 * between elements. Regardless of the object type provided by the
 * second function, {@link java.lang.Object#toString()} is used to get
 * the value to add to the list.
 *
 * To create Sets of any type of object simply use {@link Set} with
 * a specific value mapping function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class StringSet implements LongFunction<java.util.Set<String>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"StringSet(HashRange(3,7),Add(15L))", "create a set between 3 and 7 elements of String representations of Long values"})
    public StringSet(LongToIntFunction sizeFunc, LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }
    public StringSet(LongToIntFunction sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc::applyAsLong;
    }
    public StringSet(LongToIntFunction sizeFunc, LongToIntFunction valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc::applyAsInt;
    }

    public StringSet(LongFunction<?> sizeFunc, LongFunction<Object> valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc;
    }
    public StringSet(LongFunction<?> sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc::applyAsLong;
    }
    public StringSet(LongFunction<?> sizeFunc, LongToIntFunction valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc::applyAsInt;
    }

    public StringSet(LongUnaryOperator sizeFunc, LongFunction<Object> valueFunc) {
        this.sizeFunc = l -> (int) sizeFunc.applyAsLong(l);
        this.valueFunc = valueFunc;
    }
    public StringSet(LongUnaryOperator sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = l -> (int) sizeFunc.applyAsLong(l);
        this.valueFunc = valueFunc::applyAsLong;
    }
    public StringSet(LongUnaryOperator sizeFunc, LongToIntFunction valueFunc) {
        this.sizeFunc = l -> (int) sizeFunc.applyAsLong(l);
        this.valueFunc = valueFunc::applyAsInt;
    }

    private static LongToIntFunction checkSizeFunc(LongFunction<?> sizeFunc) {
        Object sizeType = sizeFunc.apply(0);
        if (int.class.isAssignableFrom(sizeType.getClass())) {
            return value -> ((LongFunction<Integer>)sizeFunc).apply(value);
        } else if (long.class.isAssignableFrom(sizeType.getClass())) {
            return value -> ((LongFunction<Long>)sizeFunc).apply(value).intValue();
        } else {
            throw new BasicError("The size function produces " + sizeType.getClass().getCanonicalName() + ", which can't be used as an integer");
        }
    }

    @Override
    public java.util.Set<String> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(valueFunc.apply(value + i).toString());
        }
        return set;
    }
}

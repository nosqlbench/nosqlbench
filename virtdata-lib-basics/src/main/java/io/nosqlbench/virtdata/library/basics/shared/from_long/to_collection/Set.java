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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.*;

import java.util.HashSet;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a {@code Set} from a long input based on two functions, the first to determine the set size, and the second to
 * populate the set with object values. The input fed to the second function is incremented between elements.
 * <p>
 * To create Sets of Strings from the String version of the same mapping functions, simply use {@link StringSet}
 * instead.
 */
@Categories({Category.collections})
@ThreadSafeMapper
@Deprecated
@DeprecatedFunction("use MapFunctions and related functions instead")
public class Set implements LongFunction<java.util.Set<Object>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"Set(HashRange(3,7),Add(15L))", "create a set between 3 and 7 elements of Long values"})
    public Set(LongToIntFunction sizeFunc,
               LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }
    public Set(LongToIntFunction sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc::applyAsLong;
    }
    public Set(LongToIntFunction sizeFunc, LongToIntFunction valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc::applyAsInt;
    }


    public Set(LongFunction<Object> sizeFunc, LongFunction<Object> valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc;
    }
    public Set(LongFunction<Object> sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc::applyAsLong;
    }
    public Set(LongFunction<Object> sizeFunc, LongToIntFunction valueFunc) {
        this.sizeFunc = checkSizeFunc(sizeFunc);
        this.valueFunc = valueFunc::applyAsInt;
    }

    public Set(LongUnaryOperator sizeFunc, LongFunction<Object> valueFunc) {
        this.sizeFunc = l -> (int) sizeFunc.applyAsLong(l);
        this.valueFunc = valueFunc;
    }
    public Set(LongUnaryOperator sizeFunc, LongUnaryOperator valueFunc) {
        this.sizeFunc = l -> (int) sizeFunc.applyAsLong(l);
        this.valueFunc = valueFunc::applyAsLong;
    }
    public Set(LongUnaryOperator sizeFunc, LongToIntFunction valueFunc) {
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
    public java.util.Set<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Set<Object> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(valueFunc.apply(value + i));
        }
        return set;
    }
}

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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_object;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashRange;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * This is a higher-order function which takes an input value,
 * and flips a coin. The first parameter is used as the threshold
 * for choosing a function. If the sample values derived from the
 * input is lower than the threshold value, then the first following
 * function is used, and otherwise the second is used.
 *
 * For example, if the threshold is 0.23, and the input value is
 * hashed and sampled in the unit interval to 0.43, then the second
 * of the two provided functions will be used.
 *
 * The input value does not need to be hashed beforehand, since the
 * user may need to use the full input value before hashing as the
 * input to one or both of the functions.
 *
 * This function will accept either a LongFunction or a {@link Function}
 * or a LongUnaryOperator in either position. If necessary, use
 * {@link java.util.function.ToLongFunction} to adapt other function forms to be
 * compatible with these signatures.
 */

@Categories(Category.distributions)
@ThreadSafeMapper
public class CoinFunc implements Function<Long, Object> {

    private final double threshold;
    private final LongFunction first;
    private final LongFunction second;
    private final HashRange cointoss = new HashRange(0.0d, 1.0d);


    @Example({"CoinFunc(0.15,NumberNameToString(),Combinations('A:1:B:23'))", "use the first function 15% of the time"})
    public CoinFunc(double threshold, Object first, Object second) {
        this.threshold = threshold;
        this.first = VirtDataFunctions.adapt(first, LongFunction.class, Object.class, true);
        this.second = VirtDataFunctions.adapt(second, LongFunction.class, Object.class, true);
    }

    @Override
    public Object apply(Long aLong) {
        double unfaircoin = cointoss.applyAsDouble(aLong);
        Object result = (unfaircoin < threshold) ? first.apply(aLong) : second.apply(aLong);
        return result;
    }

}

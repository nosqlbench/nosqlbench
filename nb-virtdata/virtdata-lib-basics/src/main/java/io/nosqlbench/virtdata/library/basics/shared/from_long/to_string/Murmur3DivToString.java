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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.murmur.Murmur3F;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.DivideToLong;

import java.util.function.LongFunction;

/**
 * Yield a String value which is the result of hashing and modulo division
 * with the specified divisor to long and then converting the value to String.
 */
@ThreadSafeMapper
@Categories({Category.general})
@DeprecatedFunction("This function is easily replaced with other simpler functions.")
public class Murmur3DivToString implements LongFunction<String> {

    private final transient ThreadLocal<Murmur3F> murmur3F_TL = ThreadLocal.withInitial(Murmur3F::new);
    private final DivideToLong divideToLongMapper;

    public Murmur3DivToString(long divisor) {
        this.divideToLongMapper = new DivideToLong(divisor);
    }

    @Override
    public String apply(long input) {
        long divided= divideToLongMapper.applyAsLong(input);
        Murmur3F murmur3f = murmur3F_TL.get();
        murmur3f.update((int) (divided % Integer.MAX_VALUE));
        return String.valueOf(murmur3f.getValue());
    }


}

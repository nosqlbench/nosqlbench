/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.murmur.Murmur3F;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.DivideToLong;

import java.util.function.LongFunction;

/**
 * Yield a String value which is the result of hashing and modulo division
 * with the specified divisor to long and then converting the value to String.
 */
@ThreadSafeMapper
@DeprecatedFunction("This function is easily replaced with other simpler functions.")
public class Murmur3DivToString implements LongFunction<String> {

    private ThreadLocal<Murmur3F> murmur3F_TL = ThreadLocal.withInitial(Murmur3F::new);
    private DivideToLong divideToLongMapper;

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

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

package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;

import java.util.function.LongFunction;

/**
 * Reads a long variable from the input, hashes and scales it
 * to the unit interval 0.0d - 1.0d, then uses the result to determine whether
 * to return null object or a loaded value.
 */
@ThreadSafeMapper
@Categories({Category.state,Category.nulls})
public class NullOrLoad implements LongFunction<Object> {

    private final String varname;
    private final double ratio;
    private final HashedDoubleRange rangefunc = new HashedDoubleRange(0.0D,1.0D);
    private final Load load;

    public NullOrLoad(double ratio, String varname) {
        if (ratio<0.0D || ratio>1.0D) {
            throw new RuntimeException("The " + NullOrLoad.class.getSimpleName() + " function requires a ratio between 0.0D and 1.0D");
        }
        this.ratio = ratio;
        load = new Load(varname);
        this.varname = varname;
    }

    @Override
    public Object apply(long basis) {
        double v = rangefunc.applyAsDouble(basis);
        if (v <= ratio) {
            return null;
        }
        return load.apply(basis); // basis doesn't matter here
    }
}

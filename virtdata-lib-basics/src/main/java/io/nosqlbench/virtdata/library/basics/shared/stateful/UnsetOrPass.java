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
import io.nosqlbench.virtdata.api.bindings.VALUE;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashRange;

import java.util.function.Function;

/**
 * Reads a long variable from the thread local variable map, hashes and scales it
 * to the unit interval 0.0d - 1.0d, then uses the result to determine whether
 * to return UNSET.value or the input value.
 */
@ThreadSafeMapper
@Categories({Category.state,Category.nulls})
public class UnsetOrPass implements Function<Object,Object> {

    private final String varname;
    private final double ratio;
    private final HashRange rangefunc = new HashRange(0.0D,1.0D);

    public UnsetOrPass(double ratio, String varname) {
        if (ratio<0.0D || ratio >1.0D) {
            throw new RuntimeException("The " + UnsetOrPass.class.getSimpleName() + " function requires a ratio between 0.0D and 1.0D");
        }
        this.ratio = ratio;
        this.varname = varname;
    }

    @Override
    public Object apply(Object o) {
        long basis;
        Object o1 = SharedState.tl_ObjectMap.get().get(varname);
        if (o1 instanceof Long) {
            basis = (Long) o1;
        } else {
            throw new RuntimeException("The NullsRatio function requires a variable to have been saved with Save('somename')");
        }
        double v = rangefunc.applyAsDouble(basis);
        if (v <= ratio) {
            return VALUE.unset;
        }
        return o;
    }
}

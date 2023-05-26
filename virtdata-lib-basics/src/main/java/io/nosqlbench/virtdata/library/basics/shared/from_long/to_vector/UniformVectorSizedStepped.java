/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Create a vector which consists of a number of uniform vector ranges.
 * Each range is set as [min,max] inclusive by a pair of double values such as 3.0d, 5.0d, ...
 * You may provide an initial integer to set the number of components in the vector.
 * After the initial (optional) size integer, you may provide odd, even pairs of min, max.
 * If a range is not specified for a component which is expected from the size, the it is
 * automatically replaced with a unit interval double variate.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class UniformVectorSizedStepped implements LongFunction<List<Double>> {

    private final int dim;
    private final LongToDoubleFunction[] funcs;

    @Example({"UniformVectorSizedStepped(3)","create a 3-component vector from unit interval variates"})
    @Example({"UniformVectorSizedStepped(1.0d,100.0d,5.0d,6.0d)","create a 2-component vector from the specified uniform ranges [1.0d,100.0d] and [5.0d,6.0d]"})
    @Example({"UniformVectorSizedStepped(2,3.0d,6.0d)","create a 2-component vector from ranges [3.0d,6.0d] and [0.0d,1.0d]"})
    public UniformVectorSizedStepped(Number... dims) {
        if (dims.length>=1 && (dims.length)%2==1 && dims[0] instanceof Integer) {
            this.dim = dims[0].intValue();
            dims = Arrays.copyOfRange(dims,1,dims.length);
        } else {
            dim=dims.length/2;
        }
        if ((dims.length%2)!=0) {
            throw new RuntimeException("Unable to set uniform range as [min,max] for pairs when count is odd. You must provide complete [min, max] value pairs as a,b,c,d,...");
        }
        this.funcs = new LongToDoubleFunction[dim];
        for (int i = 0; i < dim; i++) {
            if (i<dims.length/2) {
                funcs[i]=new HashedDoubleRange(dims[i<<1].doubleValue(),dims[(i<<1)+1].doubleValue());
            } else {
                funcs[i]=new HashedDoubleRange(0.0d,1.0d);
            }
        }
    }

    @Override
    public List<Double> apply(long value) {
        Double[] vector = new Double[dim];
        for (int idx = 0; idx < vector.length; idx++) {
            vector[idx]=funcs[idx].applyAsDouble(value+idx);
        }
        return Arrays.asList(vector);
    }
}

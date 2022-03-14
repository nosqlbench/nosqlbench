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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleInt;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbD;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Allows for easy branching over multiple functions with specific
 * weights.
 */
@Categories(Category.distributions)
@ThreadSafeMapper
public class WeightedFuncs implements LongFunction<Object> {

    private final LongFunction<Object>[] funcs;
    private final AliasSamplerDoubleInt functionSampler;
    private final HashedDoubleRange unitSampler = new HashedDoubleRange(0.0d, 1.0d);

    public WeightedFuncs(Object... weightsAndFuncs) {
        List<EvProbD> probabilities = new ArrayList<>();
        List<LongFunction<Object>> functions = new ArrayList<>();

        if ((weightsAndFuncs.length % 2) != 0) {
            throw new RuntimeException("You must have weights and functions, pairwise." +
                    "This is not possible with " + Arrays.toString(weightsAndFuncs));
        }

        for (int i = 0; i < weightsAndFuncs.length; i += 2) {

            Object w = weightsAndFuncs[i];
            double weight = 1.0d;
            try {
                weight = (double) w;
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("the 0th and ever even value must be a floating point weight.");
            }
            probabilities.add(new EvProbD(i >> 1, weight));

            Object f = weightsAndFuncs[i + 1];
            try {
                LongFunction func = VirtDataFunctions.adapt(
                        f, LongFunction.class, Object.class, true
                );
                functions.add(func);
            } catch (Exception e) {
                throw new RuntimeException("There was a problem resolving function " + f);
            }
        }
        this.funcs = functions.toArray(new LongFunction[0]);
        this.functionSampler = new AliasSamplerDoubleInt(probabilities);
    }

    @Override
    public Object apply(long value) {
        double univariate = unitSampler.applyAsDouble(value);
        int index = functionSampler.applyAsInt(univariate);
        LongFunction<Object> func = funcs[index];
        Object result = func.apply(value);
        return result;
    }
}

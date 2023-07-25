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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.util.DoubleCombiner;

import java.util.function.LongFunction;

/**
 * This is a version of the NoSQLBench {@link io.nosqlbench.virtdata.library.basics.shared.util.Combiner}
 * which is especially suited to constructing unique sequences of doubles. This can be to create arbitrarily long
 * vectors in double[] form, where each vector corresponds to a specific character encoding. Based on the
 * maximum cardinality of symbol values in each position, a step function on the unit interval is created for you
 * and used as a source of magnitudes.
 * <p>
 * For example, with a combiner spec of "{@code a-yA-Y*1024}", the "{@code }a-yA-Y" part creates a character set
 * mapping for 50 distinct indexed character values with the letter acting as a code, and then the "{@code *1024}"
 * repeats ths mapping over 1024 <em>digits</em> of values, which are then concatenated into an array of values as a
 * uniquely encoded vector. In actuality, the internal model is computed separately from the character encoding, so is
 * efficient, although the character encoding can be used to uniquely identify each vector.
 * </p>
 *
 * <p>Note that as with other combiner forms, you can specify a different cardinality for each position, although
 * the automatically computed step function for unit-interval will be based on the largest cardinality. It is not
 * computed separately for each position. Thus, a specifier like "{@code a-z*5;0-9*2}"</p> will only see the last two
 * positions using a fraction of the possible magnitudes, as the a-z element has the most steps at 26 between 0.0 and
 * 1.0.
 */
@ThreadSafeMapper
@Categories({Category.experimental, Category.premade})
public class DoubleVectors extends DoubleCombiner implements LongFunction<double[]> {
    /**
     * Create a radix-mapped vector function based on a spec of character ranges and combinations.
     * @param spec - The string specifier for a symbolic cardinality and symbol model that represents the vector values
     */
    @Example({"DoubleVector('0-9*12')","Create a sequence of vectors encoding a 10-valued step function over 12 dimensions"})
    @Example({"DoubleVector('01*1024')","Create a sequence of vectors encoding a 2-valued step function over 1024 dimensions"})
    @Example({"DoubleVector('a-yA-Y0-9!@#$%^&*()*512')","Create a sequence of vectors encoding a 70-valued step function over 512 dimensions"})
    public DoubleVectors(String spec) {
        super(spec, new DoubleCache(new UnitSteps(DoubleCombiner.maxRadixDigits(spec))));
    }
}

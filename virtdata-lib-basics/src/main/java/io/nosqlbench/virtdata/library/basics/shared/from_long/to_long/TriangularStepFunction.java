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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * <P>Compute a value which increases monotonically with respect to the cycle value.
 * All values for f(X+(m>=0)) will be equal or greater than f(X). In effect, this
 * means that with a sequence of monotonic inputs, the results will be monotonic as
 * well as clustered. The values will approximate input/average, but will vary in frequency
 * around a simple binomial distribution.</P>
 *
 * <p>The practical effect of this is to be able to compute a sequence of values
 * over inputs which can act as foreign keys, but which are effectively ordered.</p>
 *
 * <H3>Call for Ideas</H3>
 * <p>Due to the complexity of generalizing this as a pure function over other distributions,
 * this is the only function of this type for now. If you are interested in this problem
 * domain and have some suggestions for how to extend it to other distributions, please
 * join the project or let us know.</p>
 */
@ThreadSafeMapper
public class TriangularStepFunction implements LongUnaryOperator {

    private final Hash hasher = new Hash();
    private final long median;
    private final LongUnaryOperator sizer;

    private final long variance;


    @Example({"TriangularStepFunction(100,20)","Create a sequence of values where the average and median is 100, but the range of values is between 80 and 120."})
    @Example({"TriangularStepFunction(80,10)","Create a sequence of values where the average and median is 80, but the range of values is between 70 and 90."})
    TriangularStepFunction(long average, long variance) {
        if (variance < 0 || variance > average) {
            throw new RuntimeException(
                "The median must be non-negative, and the variance must be less than the median. " +
                    "You provided median=" + average + ", variance=" + variance + "."
            );
        }
        this.median = average;
        this.variance = variance;
        this.sizer = new HashRange(average-variance,average+variance);
    }

    TriangularStepFunction(long average) {
        this(average, average/2);
    }

    @Override
    public long applyAsLong(long operand) {
        // window number
        long count = operand / median;
        // offset within window
        long offset = operand % median;
        // base of window
        long base = operand - offset;
        // variate up to window size
        long variance = sizer.applyAsLong(base);
        // variate offset from start of window
        long slice = base + variance;
        // select current or next window
        long result = ((slice)>operand) ? count : count + 1;
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{median="+median+",variance="+variance+"}";
    }
}

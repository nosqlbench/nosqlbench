package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;

/// Blends two functions with a domain of 0..Long.MAX_VALUE as the input interval,
/// and a double output. The output value is interpolated between the output value
/// of the two according to the mix function. When the mix function yields a value
/// of 0.0, then the mix is turned _fully counter-clockwise_., or fully on the first provided
/// function. When the value is 1.0, the mix is turned all the clockwise, or fully on the second
/// provided function.
///
/// If there are only two inner functions provided to HashMix, then it will default to
/// sampling random mixes at a randomized sample point. In other words, the variates
/// provided will be somewhere between the two curves on the unit interval. This is a simple way
/// to sample between two curves by default. The yielded value will be greater than or equal to
/// the lower of the two values at any point, and less than or equal to the greater of either.
///
/// If a third parameter is provided to control the mix, then the mix can be set directly as a
/// unit interval. (The dial goes from 0.0 to 1.0). Any double or float value here will suffice.
/// You can use this when you want to have a test parameter that slews between two modeled
/// shapes. You can alternately provide any other function which can be coerced to a LongToDouble
/// function as a dynamic mix control. IFF such a function is provided, it must also be responsible
/// for hashing the input value if pseudo-randomness is desired.
///
/// If a fourth parameter is provided, the sample point can also be controlled. By default, the
/// values on the provided curves will be sampled pseudo-randomly. However, a fourth parameter
/// can override this just like the mix ratio. As well, if you provide a value or function
/// to control the sample point, you are also responsible for any hashing needed to sample across
/// the whole space of possible values.
///
/// The flexibility of these two parameters provides a substantial amount of flexibility. You
/// can, for example:
///
/// - sample variates between two curves
/// - sample variates at a selected morphing step between the curves
/// - sample variates between two curves on a subsection of the unit interval
/// - sample variates within a defined band gap of the two curves
@ThreadSafeMapper
@Categories(Category.functional)
public class HashMix implements LongToDoubleFunction {

    private final LongToDoubleFunction f1;
    private final LongToDoubleFunction f2;
    private final LongToDoubleFunction mixF;
    private final LongUnaryOperator sampleF;

    @Example({
        "HashMix(Func1(),Func2())",
        "yield samples between func1 and func2 values at some random random sample point x"
    })
    @Example({
        "HashMix(Func1(),Func2(),0.25d)",
        "yield samples which are 25% from the sample values for func1 and func2 at some random "
        + "sample point x"
    })
    @Example({
        "HashMix(Func1(),Func2(),HashRange(0.25d,0.75d)",
        "yield samples between 25% and 75% from func1 to func2 values at some random sample point x"
    })
    @Example({
        "HashMix(Func1(),Func2(),0.0d,ScaledDouble())",
        "access Func1 values as if it were the only one provided. ScaledDouble adds no "
        + "randomization the input value, but it does map it to the sample domain of 0.0d-0.1d."
    })
    public HashMix(Object curve1F, Object curve2F, Object mixPointF, Object samplePointF) {
        if (mixPointF instanceof Double v) {
            if (v > 1.0d || v < 0.0d) {
                throw new RuntimeException(
                    "mix value (" + v + ") must be within the unit" + " range [0.0d,1.0d]");
            }
            this.mixF = n -> v;
        } else if (mixPointF instanceof Float v) {
            if (v > 1.0d || v < 0.0d) {
                throw new RuntimeException(
                    "mix value (" + v + ") must be within the unit" + " range [0.0d,1.0d]");
            }
            this.mixF = n -> v;
        } else {
            this.mixF = VirtDataConversions.adaptFunction(mixPointF, LongToDoubleFunction.class);
        }
        this.f1 = VirtDataConversions.adaptFunction(curve1F, LongToDoubleFunction.class);
        this.f2 = VirtDataConversions.adaptFunction(curve2F, LongToDoubleFunction.class);
        this.sampleF = VirtDataConversions.adaptFunction(samplePointF, LongUnaryOperator.class);
    }

    public HashMix(Object curve1F, Object curve2F, Object mixPointF) {
        this(
            curve1F,
            curve2F,
            mixPointF,
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange(Long.MAX_VALUE)
        );
    }

    public HashMix(Object curve1F, Object curve2F) {
        this(
            curve1F,
            curve2F,
            new HashRange(0.0d, 1.0d),
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange(Long.MAX_VALUE)
        );
    }

    public HashMix(LongToDoubleFunction f1, LongToDoubleFunction f2) {
        this(
            f1,
            f2,
            new HashRange(0.0d, 1.0d),
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange(Long.MAX_VALUE)
        );
    }


    @Override
    public double applyAsDouble(long value) {
        long sampleAt = sampleF.applyAsLong(value);
        double v1 = f1.applyAsDouble(sampleAt);
        double v2 = f2.applyAsDouble(sampleAt);
        double mix = mixF.applyAsDouble(value);
        return LERP.lerp(v1, v2, mix);
    }
}

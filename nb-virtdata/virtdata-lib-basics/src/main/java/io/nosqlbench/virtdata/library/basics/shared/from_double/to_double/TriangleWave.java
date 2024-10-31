/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.DoubleUnaryOperator;
import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
@Categories(Category.periodic)
public class TriangleWave implements DoubleUnaryOperator {
    private final double phaseLength;
    private final DoubleUnaryOperator scaleFunc;
    private final DoubleUnaryOperator normalizerFunc;

    private final double halfWave;

    public TriangleWave(double phaseLength, Object scaler) {
        this.halfWave = phaseLength*0.5d;

        normalizerFunc=d -> d/(phaseLength/2.0);
        this.phaseLength=phaseLength;
        if (scaler instanceof Number number) {
            if (scaler instanceof Double adouble) {
                this.scaleFunc=d -> d*adouble;
            } else {
                this.scaleFunc= d -> d*number.doubleValue();
            }
        } else {
            this.scaleFunc = VirtDataConversions.adaptFunction(scaler, DoubleUnaryOperator.class);
        }
    }
    public TriangleWave(double phaseLength) {
        this(phaseLength, LongUnaryOperator.identity());
    }

    @Override
    public double applyAsDouble(double operand) {
        double position = operand % phaseLength;
        int slot = (int) (4.0d*position/phaseLength);
        double sample = switch (slot) {
            case 0 -> position;
            case 1 -> halfWave-position;
            case 2 -> position-halfWave;
            case 4 -> phaseLength-position;
            default -> Double.NaN;
        };
        double normalized = normalizerFunc.applyAsDouble(sample);
        double scaled = scaleFunc.applyAsDouble(sample);
        return sample;
    }
}

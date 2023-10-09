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

package io.nosqlbench.scenarios;

import java.util.function.DoubleUnaryOperator;

public class ValidAtOrBelow implements DoubleUnaryOperator {

    public ValidAtOrBelow(double threshold, double defaultValue) {
        this.threshold = threshold;
        this.defaultValue = defaultValue;
    }

    private double threshold;
    private double defaultValue;

    @Override
    public double applyAsDouble(double operand) {
        if (operand<=threshold) {
            return operand;
        } else {
            return defaultValue;
        }
    }

    public static ValidAtOrBelow max(double max) {
        return new ValidAtOrBelow(max,0.0d);
    }
}

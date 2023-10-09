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

import jakarta.validation.Valid;

import java.util.function.DoubleUnaryOperator;

public class ValidAtOrAbove implements DoubleUnaryOperator {

    public ValidAtOrAbove(double threshold, double defaultValue) {
        this.threshold = threshold;
        this.defaultValue = defaultValue;
    }

    private double threshold;
    private double defaultValue;

    @Override
    public double applyAsDouble(double operand) {
        if (operand>=threshold) {
            return operand;
        } else {
            return defaultValue;
        }
    }

    public static ValidAtOrAbove min(double min) {
        return new ValidAtOrAbove(min,0.0d);
    }
}

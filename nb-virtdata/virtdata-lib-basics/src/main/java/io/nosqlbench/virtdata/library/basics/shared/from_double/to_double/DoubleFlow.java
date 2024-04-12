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

package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.Example;

import java.util.function.DoubleUnaryOperator;

/**
 * Combine multiple DoubleUnaryOperators into a single function.
 */
public class DoubleFlow implements DoubleUnaryOperator {

    private final DoubleUnaryOperator[] ops;

    @Example({"StringFlow(Add(3.0D),Mul(10.0D))","adds 3.0 and then multiplies by 10.0"})
    public DoubleFlow(DoubleUnaryOperator... ops) {
        this.ops = ops;
    }

    @Override
    public double applyAsDouble(double operand) {
        double value = operand;
        for (DoubleUnaryOperator op : ops) {
            value = op.applyAsDouble(value);
        }
        return value;
    }
}

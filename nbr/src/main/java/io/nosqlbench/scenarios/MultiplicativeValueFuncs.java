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

public class MultiplicativeValueFuncs {

    /**
     * If the value is above the threshold, return the value. If not, return 1.0d;
     * @param threshold
     * @return
     */
    public static DoubleUnaryOperator above(double threshold) {
        return (v) -> (v<threshold) ? 1.0d : v;
    }

    public static DoubleUnaryOperator between(double min, double max) {
        return (v) -> (v>=min & v<=max) ? v : 1.0d;
    }

    public static DoubleUnaryOperator below(double threshold) {
        return (v) -> (v>threshold) ? 1.0d : v;
    }

    public static DoubleUnaryOperator exp_2() {
        return (v) -> (v*v)+1;
    }

    public static DoubleUnaryOperator exp_e() {
        return Math::exp;
    }
}

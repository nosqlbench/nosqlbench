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

public class ValueFuncs {

    public static double zeroBelow(double value, double threshold) {
        if (value<threshold) {
            return 0.0d;
        }
        return value;
    }

    public static double zeroAbove(double value, double threshold) {
        if (value>threshold) {
            return 0.0d;
        }
        return value;
    }

    /**
     * Apply exponential weighting to the value base 2. For rate=1.0, the weight
     */
    public static double exp_2(double value) {
        return (value*value)+1;
    }

    public static double exp_e(double value) {
        return Math.exp(value);
    }
}

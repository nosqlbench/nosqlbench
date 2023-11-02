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

package io.nosqlbench.scenarios.simframe.stabilization;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.ToDoubleFunction;

public class StatFunctions {
    /**
     * A continuous sigmoid function which looks very close to a step function at the inflection points.
     * This provides a navigable surface for optimizers while also acting like an all-or none filter.
     * This function yields 0.0d or 1.0d over most of its domain, except between shelf-0.0025 and shelf,
     * where it is a steep sigmoid. Specifically, at shelf, the value is 1.0; at shelf-0.0001, the value is 0.9999,
     * dropping quickly on the lower side of shelf, leveling out at 0.0 at shelf-0.0025.
     * @param input x
     * @param lowcut The point on the x axis at which all higher values should yield 1.0
     */
    public static double sigmoidE4HighPass(double input, double lowcut) {
        return 1.0d/(1.0d+Math.pow(Math.E,(-10000.0d*(input-(lowcut-0.001d)))));
    }
    /**
     * Like {@link #sigmoidE4HighPass, but inverted with respect to the Y axis. This has the same roll-off charater
     * where the high (1.0) shelf in included through the cutoff value.
     * @param input x
     * @param lowcut The point on the x axis at which all lower values should yield 1.0
     */
    public static double sigmoidE4LowPass(double input, double highcut) {
        double v = 1.0d/(1.0d + Math.pow(Math.E, (10000.0d * (input - (highcut + 0.001d)))));
        return v;
    }

}

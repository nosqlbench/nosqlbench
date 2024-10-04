/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.math4.legacy.distribution.EnumeratedIntegerDistribution;

/**
 * Create a sampler based on enumeration of integer values an sample over them
 * using the EnumeratedInts distribution curve provided by Apache Commons Math.
 * This version will roughly produce the distribution, but since it also relies on
 * interpolation by default, non-step values may appear at low frequencies. If this
 * is a desired effect, then this function is suitable. For example: consider this
 * result:
 * <pre>{@code
 *  nb5 run driver=stdout op="{{EnumeratedInts('10:10 20:20 30:30 40:40')}}\n" cycles=10000 | sort -n | uniq -c
 *       1 STDOUT0 (pending,current,complete)=(0,0,10000) 100.00% (last report)
 *       1 9
 *    1036 10
 *       2 11
 *       2 13
 *       1 14
 *       3 15
 *       2 16
 *       1 18
 *       1 19
 *    1937 20
 *       1 21
 *       1 23
 *       1 24
 *       1 25
 *       1 28
 *       1 29
 *    3077 30
 *       1 31
 *       1 33
 *       1 34
 *       2 35
 *       1 37
 *       1 39
 *    3924 40
 * }</pre>
 *
 * The values here which are not multiples of 10 are not specified, yet the appear. For some testing, this is
 * helpful as a fuzzer, but for more precise step-value sampling, see {@link AliasSampler}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class EnumeratedInts extends IntToIntDiscreteCurve {
    public EnumeratedInts(String data, String... mods) {
        super(new EnumeratedIntegerDistribution(parseIntLabels(data), parseDoubleWeights(data)), mods);
    }

    public static int[] parseIntLabels(String input) {
        String[] entries = input.split("[;, ]");
        int[] elements = new int[entries.length];
        String[] parts;
        for (int i = 0; i < entries.length; i++) {
            parts = entries[i].split(":");
            elements[i] = Integer.parseInt(parts[0]);
        }
        return elements;
    }

    public static double[] parseDoubleWeights(String input) {
        String[] entries = input.split("[;, ]");
        double[] weights = new double[entries.length];
        String[] parts;
        for (int i = 0; i < entries.length; i++) {
            parts = entries[i].split(":");
            weights[i] = parts.length==2 ? Double.parseDouble(parts[1]) : 1.0d;
        }
        return weights;
    }


}

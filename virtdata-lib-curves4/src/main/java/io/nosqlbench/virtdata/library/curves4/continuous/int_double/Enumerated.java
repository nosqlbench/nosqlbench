package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

/*
 * Copyright (c) 2022 nosqlbench
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
import org.apache.commons.math4.distribution.EnumeratedRealDistribution;

/**
 * Creates a probability density given the values and optional weights provided, in "value:weight value:weight ..." form.
 * The weight can be elided for any value to use the default weight of 1.0d.
 *
 * @see <a href="http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/distribution/EnumeratedRealDistribution.html">Commons JavaDoc: EnumeratedRealDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Enumerated extends IntToDoubleContinuousCurve {

    @Example({"Enumerated('1 2 3 4 5 6')", "a fair six-sided die roll",
    "[1-10]/"})
    @Example({"Enumerated('1:2.0 2 3 4 5 6')", "an unfair six-sided die roll, where 1 has probability mass 2.0, and everything else has only 1.0",})
    public Enumerated(String data, String... mods) {
        super(new EnumeratedRealDistribution(parseWeights(data)[0],parseWeights(data)[1]), mods);
    }

    private static double[][] parseWeights(String input) {
        String[] entries = input.split("[;, ]");
        double[][] elements = new double[2][entries.length];
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split(":");
            elements[1][i]=1.0d;
            switch(parts.length) {
                case 2:
                    elements[1][i] = Double.parseDouble(parts[1]);
                case 1:
                    elements[0][i] = Double.parseDouble(parts[0]);
                    break;
                default:
                    throw new RuntimeException("Unable to parse entry or weight from '" + entries[i] + "'");
            }
        }
        return elements;
    }
}

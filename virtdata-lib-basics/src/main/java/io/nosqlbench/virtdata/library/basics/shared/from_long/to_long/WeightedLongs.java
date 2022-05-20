package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

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
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Provides a long value from a list of weighted values. The total likelihood
 * of any value to be produced is proportional to its relative weight in
 * the total weight of all elements.
 *
 * This function automatically hashes the input, so the result is already pseudo-random.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class WeightedLongs implements LongFunction<Long> {

    private final String valuesAndWeights;
    private double[] unitWeights; // Positional weights after parsing and unit weight normalization
    private double[] cumulativeWeights;
    private final HashedDoubleRange unitRange = new HashedDoubleRange(0.0D, 1.0D);
    private long[] values;

    @Example({"WeightedLongs('1:10;3;5;12345;1","Yield 1 62.5% of the time, 3 31.25% of the time, and 12345 6.2% of the time"})
    @Example({"WeightedLongs('1,6,7","Yield 1 33.3% of the time, 6 33.3% of the time, and 7 33.3% of the time"})
    public WeightedLongs(String valuesAndWeights) {
        this.valuesAndWeights = valuesAndWeights;
        parseWeights();
    }

    private void parseWeights() {
        String[] pairs = valuesAndWeights.split(";");
        if (pairs.length == 0) {
            throw new RuntimeException("No pairs were found. They must be separated by ';'");
        }

        String[] fragments = new String[pairs.length];
        List<Double> parsedWeights = new ArrayList<>();
        values = new long[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i].split(":", 2);
            if (pair.length == 2) {
                parsedWeights.add(Double.valueOf(pair[1].trim()));
            } else {
                parsedWeights.add(1.0D);
            }
            values[i] = Long.parseLong(pair[0].trim());
        }
        double total = parsedWeights.stream().mapToDouble(f -> f).sum();
        unitWeights = parsedWeights.stream().mapToDouble(f -> f / total).toArray();
        cumulativeWeights = new double[unitWeights.length];
        double cumulative = 0.0D;
        for (int i = 0; i < unitWeights.length; i++) {
            cumulative += unitWeights[i];
            cumulativeWeights[i] = cumulative;
        }
    }

    @Override
    public Long apply(long value) {
        double sampledUnit = unitRange.applyAsDouble(value);
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (sampledUnit < cumulativeWeights[i]) {
                return values[i];
            }
        }
        throw new RuntimeException(
                "sampled value '" + sampledUnit + "' was not below final cumulative weight: "
                        + cumulativeWeights[cumulativeWeights.length - 1]);
    }
}

package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_double.HashedDoubleRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Allows for weighted elements to be used, such as
 * <code>a:0.25;b:0.25;c:0.5</code> or <code>a:1;b:1.0;c:2.0</code>
 * The unit weights are normalized to the cumulative sum
 * internally, so it is not necessary for them
 * to add up to any particular value.
 */
@ThreadSafeMapper
public class WeightedStrings implements LongFunction<String> {

    private final String valuesAndWeights;
    private double[] unitWeights; // Positional weights after parsing and unit weight normalization
    private double[] cumulativeWeights;
    private HashedDoubleRange unitRange = new HashedDoubleRange(0.0D, 1.0D);
    private String[] values;

    public WeightedStrings(String valuesAndWeights) {
        this.valuesAndWeights = valuesAndWeights;
        parseWeights();
    }

    private void parseWeights() {
        String[] pairs = valuesAndWeights.split("[;,]");
        if (pairs.length == 0) {
            throw new RuntimeException("No pairs were found. They must be separated by ';'");
        }

        values = new String[pairs.length];
        List<Double> parsedWeights = new ArrayList<>();
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i].split(":", 2);
            if (pair.length == 2) {
                parsedWeights.add(Double.valueOf(pair[1].trim()));
            } else {
                parsedWeights.add(1.0D);
            }
            values[i] = pair[0].trim();
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
    public String apply(long value) {
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

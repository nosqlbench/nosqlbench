package io.virtdata.continuous.long_double;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.math4.distribution.EnumeratedRealDistribution;

@ThreadSafeMapper
public class Enumerated extends LongToDoubleContinuousCurve {

    @Example({"Enumerated('1 2 3 4 5 6')","a fair 6-sided die"})
    @Example({"Enumerated('1:2.0 2 3 4 5:0.5 6:0.5')","an unfair fair 6-sided die, where ones are twice as likely, and fives and sixes are half as likely"})
    public Enumerated(String data, String... mods) {
        super(new EnumeratedRealDistribution(parseWeights(data)[0], parseWeights(data)[1]), mods);
    }

    private static double[][] parseWeights(String input) {
        String[] entries = input.split("[;, ]");
        double[][] elements = new double[2][entries.length];
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split(":");
            elements[1][i] = 1.0d;
            switch (parts.length) {
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

package io.virtdata.libbasics.shared.from_long.to_object;

import io.virtdata.libbasics.shared.from_long.to_long.FixedValues;
import org.junit.Test;

import java.util.Arrays;

public class WeightedFuncsTest {

    @Test
    public void testFuncSelectionDistribution() {
        WeightedFuncs f = new WeightedFuncs(
                1.0d, new FixedValues(0L),
                1.0d, new FixedValues(1L),
                1.0d, new FixedValues(2L),
                1.0d, new FixedValues(3L),
                1.0d, new FixedValues(4L),
                1.0d, new FixedValues(5L),
                1.0d, new FixedValues(6L),
                1.0d, new FixedValues(7L),
                1.0d, new FixedValues(8L),
                1.0d, new FixedValues(9L)
        );
        long[] results = new long[10];
        
        for (int i = 0; i < 1000000; i++) {
            Object o = f.apply(i);
            int v = ((Long) o).intValue();
            results[v]++;
        }
        System.out.print(Arrays.toString(results));

    }

}
package io.virtdata.libbasics.shared.from_long.to_object;

import io.virtdata.libbasics.shared.from_long.to_long.HashRange;
import io.virtdata.libbasics.shared.from_long.to_string.Combinations;
import io.virtdata.libbasics.shared.from_long.to_string.NumberNameToString;
import io.virtdata.libbasics.shared.functionadapters.ToLongFunction;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CoinFuncTest {

    // sanity checks on ctor usage only
    @Test
    public void testToLongCtor() {
        CoinFunc f = new CoinFunc(0.23, new ToLongFunction(new HashRange(0,3)), new Combinations("0-9"));
        Object r = f.apply(3L);
    }

    // sanity checks on ctor usage only
    @Test
    public void testLongFuncCtor() {
        CoinFunc f = new CoinFunc(0.23, new NumberNameToString(), new Combinations("0-9"));
        Object r = f.apply(3L);
    }

    // Uncomment this if you want to see the qualitative check
    @Ignore
    @Test
    public void testResults() {
        CoinFunc f = new CoinFunc(0.1,
                new ToLongFunction(new HashRange(0L,10L)),
                new ToLongFunction(new HashRange(10L,100L))
        );

        long[] counts = new long[100];
        for (int i = 0; i < 10000; i++) {
            Object r = f.apply((long) i);
            int value = ((Long)r).intValue();
            counts[value] = counts[value]+1;
        }
        String summary = Arrays.stream(counts).mapToObj(String::valueOf).collect(Collectors.joining(","));
        System.out.println(summary);
    }

}
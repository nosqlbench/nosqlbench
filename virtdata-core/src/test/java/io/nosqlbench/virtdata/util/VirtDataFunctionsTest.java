package io.nosqlbench.virtdata.util;

import io.nosqlbench.virtdata.api.VirtDataFunctions;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.function.Function;
import java.util.function.LongFunction;

public class VirtDataFunctionsTest  {

    @Test
    public void testLongUnaryConversion() {
        Function<Long,Long> fl = (Long l) -> Math.max(l,43L);
        LongFunction<Long> adapted = VirtDataFunctions.adapt(fl, LongFunction.class, long.class, true);
        long f2 = adapted.apply(42L);
    }

    @Test(expected = InvalidParameterException.class)
    public void testWrongLongUnaryConversion() {
        Function<Long,Integer> fl = (Long l) -> Math.max(l.intValue(),43);
        LongFunction<Long> adapted = VirtDataFunctions.adapt(fl, LongFunction.class, Long.class, true);
        long f2 = adapted.apply(42L);
    }

}

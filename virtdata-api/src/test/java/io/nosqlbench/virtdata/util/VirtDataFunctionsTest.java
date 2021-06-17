package io.nosqlbench.virtdata.util;

import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.function.Function;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class VirtDataFunctionsTest  {

    @Test
    public void testLongUnaryConversion() {
        Function<Long,Long> fl = (Long l) -> Math.max(l,43L);
        LongFunction<Long> adapted = VirtDataFunctions.adapt(fl, LongFunction.class, long.class, true);
        long f2 = adapted.apply(42L);
    }

    @Test
    public void testWrongLongUnaryConversion() {
        Function<Long,Integer> fl = (Long l) -> Math.max(l.intValue(), 43);
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> VirtDataFunctions.adapt(fl, LongFunction.class, Long.class, true));
    }
}

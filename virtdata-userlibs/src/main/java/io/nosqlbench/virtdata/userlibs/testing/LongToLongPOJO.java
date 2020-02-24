package io.nosqlbench.virtdata.userlibs.testing;

import java.util.function.LongFunction;

public class LongToLongPOJO implements LongFunction<ARandomPOJO> {

    @Override
    public ARandomPOJO apply(long value) {
        return new ARandomPOJO(value);
    }
}

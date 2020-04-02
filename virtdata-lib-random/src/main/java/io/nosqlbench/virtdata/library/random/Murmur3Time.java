package io.nosqlbench.virtdata.library.random;


import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.util.function.LongUnaryOperator;

/**
 * A generator that is mostly useless, except for testing useless generators.
 * This is used as a control for the concurrent generation tester.
 */
public class Murmur3Time implements LongUnaryOperator {
    private Murmur3F murmur3F = new Murmur3F(Thread.currentThread().getName().hashCode());
    @Override
    public long applyAsLong(long operand) {
        murmur3F.updateLongLE(System.nanoTime());
        return murmur3F.getValue();
    }
}

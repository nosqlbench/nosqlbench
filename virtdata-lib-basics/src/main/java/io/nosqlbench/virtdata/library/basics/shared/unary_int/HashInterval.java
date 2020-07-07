package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class HashInterval implements IntUnaryOperator {

    private final int minValue;
    private final int  width;
    private final Hash hash = new Hash();


    /**
     * Create a hash interval based on a minimum value of 0 and a specified width.
     * @param width The maximum value, which is excluded.
     */
    @Example({"HashInterval(4)","return values which could include 0, 1, 2, 3, but not 4"})
    public HashInterval(int width) {
        this.minValue=0;
        this.width=width;
    }

    /**
     * Create a hash interval
     * @param minIncl The minimum value, which is included
     * @param maxExcl The maximum value, which is excluded
     */
    @Example({"HashInterval(2,5)","return values which could include 2, 3, 4, but not 5"})
    public HashInterval(int minIncl, int maxExcl) {
        if (maxExcl<=minIncl) {
            throw new BasicError("HashInterval must have min and max value in that order, where the min is less than the max.");
        }
        this.minValue = minIncl;
        this.width = (maxExcl - minIncl);
    }

    @Override
    public int applyAsInt(int operand) {
        return minValue + (hash.applyAsInt(operand) & width);
    }
}

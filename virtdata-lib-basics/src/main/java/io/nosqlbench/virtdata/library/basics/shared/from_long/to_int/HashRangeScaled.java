package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class HashRangeScaled implements LongToIntFunction {

    private final Hash hash = new Hash();
    private final double scalefactor;

    public HashRangeScaled(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public HashRangeScaled() {
        this.scalefactor = 1.0D;
    }


    @Override
    public int applyAsInt(long operand) {
        if (operand == 0) {
            return 0;
        }
        long l = hash.applyAsLong(operand);
        return (int) (((l % operand) * scalefactor) % Integer.MAX_VALUE);
    }
}

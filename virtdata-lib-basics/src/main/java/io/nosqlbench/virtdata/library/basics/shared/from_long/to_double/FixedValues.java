package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongToDoubleFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class FixedValues implements LongToDoubleFunction {

    private final long[] fixedValues;

    @Example({"FixedValues(3D,53D,73d)", "Yield 3D, 53D, 73D, 3D, 53D, 73D, 3D, ..."})
    public FixedValues(long... values) {
        this.fixedValues = values;
    }

    @Override
    public double applyAsDouble(long value) {
        int index = (int) (value % Integer.MAX_VALUE) % fixedValues.length;
        return fixedValues[index];
    }
}

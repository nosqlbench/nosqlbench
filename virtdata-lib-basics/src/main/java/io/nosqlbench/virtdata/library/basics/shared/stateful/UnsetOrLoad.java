package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;

import java.util.function.LongFunction;

/**
 * Reads a long variable from the input, hashes and scales it
 * to the unit interval 0.0d - 1.0d, then uses the result to determine whether
 * to return UNSET.value or a loaded value.
 */
@ThreadSafeMapper
@Categories({Category.state,Category.nulls})
public class UnsetOrLoad implements LongFunction<Object> {

    private final String varname;
    private double ratio;
    private final HashedDoubleRange rangefunc = new HashedDoubleRange(0.0D,1.0D);
    private final Load load;

    public UnsetOrLoad(double ratio, String varname) {
        if (ratio<0.0D || ratio>1.0D) {
            throw new RuntimeException("The " + UnsetOrLoad.class.getSimpleName() + " function requires a ratio between 0.0D and 1.0D");
        }
        this.ratio = ratio;
        load = new Load(varname);
        this.varname = varname;
    }

    @Override
    public Object apply(long basis) {
        double v = rangefunc.applyAsDouble(basis);
        if (v <= ratio) {
            return VALUE.unset;
        }
        return load.apply(basis); // basis doesn't matter here
    }
}

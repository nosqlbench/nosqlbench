package io.nosqlbench.virtdata.library.basics.shared.from_long.to_object;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;
import io.nosqlbench.virtdata.api.VirtDataFunctions;

import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * This is a higher-order function which takes an input value,
 * and flips a coin. The first parameter is used as the threshold
 * for choosing a function. If the sample values derived from the
 * input is lower than the threshold value, then the first following
 * function is used, and otherwise the second is used.
 *
 * For example, if the threshold is 0.23, and the input value is
 * hashed and sampled in the unit interval to 0.43, then the second
 * of the two provided functions will be used.
 *
 * The input value does not need to be hashed beforehand, since the
 * user may need to use the full input value before hashing as the
 * input to one or both of the functions.
 *
 * This function will accept either a LongFunction or a {@link Function}
 * or a LongUnaryOperator in either position. If necessary, use
 * {@link java.util.function.ToLongFunction} to adapt other function forms to be
 * compatible with these signatures.
 */

@Categories(Category.distributions)
@ThreadSafeMapper
public class CoinFunc implements Function<Long, Object> {

    private final double threshold;
    private final LongFunction first;
    private final LongFunction second;
    private final HashedDoubleRange cointoss = new HashedDoubleRange(0.0d, 1.0d);


    @Example({"CoinFunc(0.15,NumberNameToString(),Combinations('A:1:B:23'))", "use the first function 15% of the time"})
    public CoinFunc(double threshold, Object first, Object second) {
        this.threshold = threshold;
        this.first = VirtDataFunctions.adapt(first, LongFunction.class, Object.class, true);
        this.second = VirtDataFunctions.adapt(second, LongFunction.class, Object.class, true);
    }

    @Override
    public Object apply(Long aLong) {
        double unfaircoin = cointoss.applyAsDouble(aLong);
        Object result = (unfaircoin < threshold) ? first.apply(aLong) : second.apply(aLong);
        return result;
    }

}

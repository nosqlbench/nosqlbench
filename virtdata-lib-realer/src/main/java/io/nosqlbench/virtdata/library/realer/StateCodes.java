package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

import java.util.function.LongFunction;

/**
 * Return a valid state code. (abbreviation)
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class StateCodes extends CSVSampler implements LongFunction<String> {

    @Example("StateCodes()")
    public StateCodes() {
        super("state_id","n/a","name","simplemaps/uszips");
    }

}

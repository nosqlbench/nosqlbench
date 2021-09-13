package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

import java.util.function.LongFunction;

/**
 * Return a valid country name.
 */
@Categories(Category.premade)
@ThreadSafeMapper
public class CountryNames extends CSVSampler implements LongFunction<String> {

    @Example("CountryNames()")
    public CountryNames() {
        super("COUNTRY_NAME","n/a","name","countries.csv");
    }

}

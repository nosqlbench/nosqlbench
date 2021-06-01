package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

import java.util.function.LongFunction;

/**
 * Return a valid country code.
 */
@Categories(Category.premade)
@ThreadSafeMapper
public class CountryCodes extends CSVSampler implements LongFunction<String> {

    @Example("CountryCodes()")
    public CountryCodes() {
        super("COUNTRY_CODE","n/a","name","countries.csv");
    }

}

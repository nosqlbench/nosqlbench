package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a county name weighted by total population.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class CountiesByPopulation extends CSVSampler {
    @Example("CountiesByPopulation()")
    public CountiesByPopulation() {
        super("county_name","population","data/simplemaps/uszips.csv");
    }
}

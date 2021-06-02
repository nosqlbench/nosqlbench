package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a city name, weighted by population density.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class CitiesByDensity extends CSVSampler {
    @Example("CitiesByDensity()")
    public CitiesByDensity() {
        super("city","density","simplemaps/uszips");
    }
}

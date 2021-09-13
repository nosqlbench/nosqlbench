package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a state name, weighted by population density.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class TimeZonesByDensity extends CSVSampler {
    @Example("TimezonesByDensity")
    public TimeZonesByDensity() {
        super("timezone","population","simplemaps/uszips");
    }
}

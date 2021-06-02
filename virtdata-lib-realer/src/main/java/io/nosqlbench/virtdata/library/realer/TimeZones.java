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
public class TimeZones extends CSVSampler {
    @Example("Timezones()")
    public TimeZones() {
        super("timezone","n/a","name","simplemaps/uszips");
    }
}

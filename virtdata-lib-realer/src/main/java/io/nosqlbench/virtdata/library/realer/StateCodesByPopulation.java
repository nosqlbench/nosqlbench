package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a state code (abbreviation), weighted by population.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class StateCodesByPopulation extends CSVSampler {
    @Example("StateCodesByPopulation()")
    public StateCodesByPopulation() {
        super("state_id","population","data/simplemaps/uszips.csv");
    }
}

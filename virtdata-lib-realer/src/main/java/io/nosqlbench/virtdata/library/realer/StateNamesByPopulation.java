package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a state name, weighted by total population.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class StateNamesByPopulation extends CSVSampler {
    @Example("StateNamesByPopulation()")
    public StateNamesByPopulation() {
        super("state_name","population","data/simplemaps/uszips.csv");
    }
}

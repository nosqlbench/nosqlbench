package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a valid county name.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class Counties extends CSVSampler {
    @Example("Counties()")
    public Counties() {
        super("city","n/a","name","simplemaps/uszips");
    }
}

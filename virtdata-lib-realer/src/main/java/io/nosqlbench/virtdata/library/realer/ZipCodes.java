package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVSampler;

/**
 * Return a valid zip code.
 */
@ThreadSafeMapper
@Categories(Category.premade)
public class ZipCodes extends CSVSampler {
    @Example("ZipCodes()")
    public ZipCodes() {
        super("zip","n/a","name","data/simplemaps/uszips.csv");
    }
}

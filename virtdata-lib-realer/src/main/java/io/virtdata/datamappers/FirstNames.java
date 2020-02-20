package io.virtdata.datamappers;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.distributions.WeightedStringsFromCSV;

import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly sampled first name from the last US census data on first names
 * occurring more than 100 times. Both male and female names are combined in this function.
 */
@ThreadSafeMapper
@Categories({Category.premade})
public class FirstNames extends WeightedStringsFromCSV implements LongFunction<String> {

    @Example({"FirstNames()","select a random first name based on the chance of seeing it in the census data"})
    public FirstNames() {
        super("Name", "Weight", "data/female_firstnames", "data/male_firstnames");
    }
    @Example({"FirstNames('map')","select over the first names by probability as input varies from 1L to Long.MAX_VALUE"})
    public FirstNames(String modifier) {
        super("Name", "Weight", modifier, "data/female_firstnames", "data/male_firstnames");
    }
}

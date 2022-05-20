package io.nosqlbench.virtdata.library.realer;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.WeightedStringsFromCSV;

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

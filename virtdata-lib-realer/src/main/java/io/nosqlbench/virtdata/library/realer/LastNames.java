/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.distributions.WeightedStringsFromCSV;

import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly sampled last name from the last US census data on last names
 * occurring more than 100 times.
 */
@ThreadSafeMapper
@Categories({Category.premade})
public class LastNames extends WeightedStringsFromCSV implements LongFunction<String> {

    @Example({"LastNames()","select a random last name based on the chance of seeing it in the census data"})
    public LastNames() {
        super("Name", "prop100k", "data/surnames");
    }
    @Example({"LastNames('map')","select over the last names by probability as input varies from 1L to Long.MAX_VALUE"})
    public LastNames(String modifier) {
        super("Name", modifier, "prop100k", "data/surnames");
    }

}

/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.random;

import io.nosqlbench.virtdata.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.api.VirtDataResources;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * TODO: Redo this a functional with murmur3F
 */
@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomLineToString implements LongFunction<String> {
    private final static Logger logger  = LogManager.getLogger(RandomLineToString.class);private List<String> lines = new ArrayList<>();

    private final MersenneTwister rng;
    private final IntegerDistribution itemDistribution;
    private final String filename;

    public RandomLineToString(String filename) {
        this.rng = new MersenneTwister(System.nanoTime());
        this.filename = filename;
        this.lines = VirtDataResources.readDataFileLines(filename);
        itemDistribution= new UniformIntegerDistribution(rng, 0, lines.size()-2);
    }

    public RandomLineToString(String filename, MersenneTwister rng) {
        this.rng = rng;
        this.filename = filename;
        this.lines = VirtDataResources.readDataFileLines(filename);
        this.lines = VirtDataResources.readDataFileLines(filename);
        itemDistribution= new UniformIntegerDistribution(rng, 0, lines.size()-2);
    }

    public RandomLineToString(String filename, long seed) {
        this.rng = new MersenneTwister(seed);
        this.filename = filename;
        this.lines = VirtDataResources.readDataFileLines(filename);
        itemDistribution= new UniformIntegerDistribution(rng, 0, lines.size()-2);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public String apply(long operand) {
        int itemIdx = itemDistribution.sample();
        String item = lines.get(itemIdx);
        return item;
    }

}

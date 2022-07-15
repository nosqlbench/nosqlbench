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

package io.nosqlbench.virtdata.library.random;

import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.function.LongToIntFunction;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomLineToInt implements LongToIntFunction {
    private final static Logger logger  = LogManager.getLogger(RandomLineToInt.class);
    private final List<String> lines;

    private final MersenneTwister rng;
    private final IntegerDistribution itemDistribution;
    private final String filename;

    public RandomLineToInt(String filename) {
        this(filename, System.nanoTime());
    }

    public RandomLineToInt(String filename, long seed) {
        this.filename = filename;
        this.lines = NBIO.readLines(filename);
        this.rng = new MersenneTwister(seed);
        this.itemDistribution= new UniformIntegerDistribution(rng, 0, lines.size()-2);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public int applyAsInt(long value) {
        int itemIdx = itemDistribution.sample();
        String item = lines.get(itemIdx);
        return Integer.valueOf(item);
    }
}


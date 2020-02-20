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

package io.virtdata.random;


import io.virtdata.annotations.DeprecatedFunction;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomLineToStringMap implements LongFunction<Map<String,String>> {

    private final RandomLineToString lineDataMapper;
    private final IntegerDistribution sizeDistribution;
    private final MersenneTwister rng;

    public RandomLineToStringMap(String paramFile, int maxSize) {
        rng = new MersenneTwister(System.nanoTime());
        this.sizeDistribution = new UniformIntegerDistribution(rng, 0,maxSize-1);
        this.lineDataMapper = new RandomLineToString(paramFile);
    }

    public RandomLineToStringMap(String paramFile, int maxSize, long seed) {
        this.rng = new MersenneTwister(seed);
        this.sizeDistribution = new UniformIntegerDistribution(rng, 0,maxSize-1);
        this.lineDataMapper = new RandomLineToString(paramFile);
    }

    @Override
    public Map<String, String> apply(long input) {
        int mapSize = sizeDistribution.sample();
        Map<String,String> map = new HashMap<>();
        for (int idx=0;idx<mapSize;idx++) {
            map.put(lineDataMapper.apply(input), lineDataMapper.apply(input));
        }
        return map;
    }

}

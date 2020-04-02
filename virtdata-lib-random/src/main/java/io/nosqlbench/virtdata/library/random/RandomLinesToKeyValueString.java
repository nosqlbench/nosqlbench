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

import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomLinesToKeyValueString implements LongFunction<String> {
    private static final Logger logger = LogManager.getLogger(RandomLinesToKeyValueString.class);

    private final RandomLineToStringMap lineDataMapper;
    private final MersenneTwister rng;

    public RandomLinesToKeyValueString(String paramFile, int maxSize) {
        this(paramFile,maxSize,System.nanoTime());
    }

    public RandomLinesToKeyValueString(String paramFile, int maxsize, long seed) {
        rng = new MersenneTwister(seed);
        lineDataMapper = new RandomLineToStringMap(paramFile, maxsize);
    }

    @Override
    public String apply(long input) {
        Map<String, String> stringStringMap = lineDataMapper.apply(input);
        String mapstring = stringStringMap.entrySet().stream().
                map(es -> es.getKey() + ":" + es.getValue() + ";")
                .collect(Collectors.joining());
        return mapstring;
    }

}

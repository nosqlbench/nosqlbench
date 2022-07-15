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

import java.nio.CharBuffer;
import java.util.function.LongFunction;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomFileExtractToString implements LongFunction<String> {

    private final static Logger logger  = LogManager.getLogger(RandomFileExtractToString.class);

    private static CharBuffer fileDataImage =null;

    private final int minsize;
    private final int maxsize;
    private final MersenneTwister rng;
    private final IntegerDistribution sizeDistribution;
    private final IntegerDistribution positionDistribution;
    private final String fileName;

    public RandomFileExtractToString(String fileName, int minsize, int maxsize) {
        this(fileName, minsize,maxsize,System.nanoTime());
    }

    public RandomFileExtractToString(String fileName, int minsize, int maxsize, long seed) {
        this.fileName = fileName;
        this.minsize = minsize;
        this.maxsize = maxsize;
        loadData();
        this.rng = new MersenneTwister(seed);
        this.sizeDistribution = new UniformIntegerDistribution(rng, minsize, maxsize);
        this.positionDistribution = new UniformIntegerDistribution(rng, 1, fileDataImage.limit() - maxsize);
    }

    private void loadData() {
        if (fileDataImage == null) {
            synchronized (RandomFileExtractToString.class) {
                if (fileDataImage == null) {
                    CharBuffer image= NBIO.readCharBuffer(fileName);
                    fileDataImage = image;
                }
            }
        }
    }

    @Override
    public String apply(long input) {


        int offset = positionDistribution.sample();
        int length = sizeDistribution.sample();
        String sub = null;
        try {
            sub = fileDataImage.subSequence(offset, offset + length).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sub;

    }

    public String toString() {
        return getClass().getSimpleName() + ":" + minsize + ":" + maxsize;
    }

}

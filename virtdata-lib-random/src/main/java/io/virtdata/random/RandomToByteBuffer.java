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
import org.apache.commons.math3.random.MersenneTwister;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomToByteBuffer implements LongFunction<ByteBuffer> {

    private final MersenneTwister rng;
    private int length;

    public RandomToByteBuffer(int length) {
        this.length = length;
        rng = new MersenneTwister(System.nanoTime());
    }

    public RandomToByteBuffer(int length, long seed) {
        this.length = length;
        rng = new MersenneTwister(seed);
    }

    @Override
    public ByteBuffer apply(long input) {
        byte[] buffer = new byte[length];
        rng.nextBytes(buffer);
        return ByteBuffer.wrap(buffer);
    }

}

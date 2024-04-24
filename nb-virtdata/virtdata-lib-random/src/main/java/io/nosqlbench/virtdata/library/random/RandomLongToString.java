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

import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.function.LongFunction;

@DeprecatedFunction("random mappers are not deterministic. They will be replaced with hash-based functions.")
public class RandomLongToString implements LongFunction<String> {
    private final MersenneTwister theTwister;

    public RandomLongToString() {
        this(System.nanoTime());
    }

    public RandomLongToString(long seed) {
        theTwister = new MersenneTwister(seed);
    }

    @Override
    public String apply(long input) {
        return String.valueOf(Math.abs(theTwister.nextLong()));
    }

}

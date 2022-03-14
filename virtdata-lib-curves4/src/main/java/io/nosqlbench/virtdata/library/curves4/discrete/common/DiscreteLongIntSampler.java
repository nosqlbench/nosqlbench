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

package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.LongToIntFunction;

public class DiscreteLongIntSampler implements LongToIntFunction {

    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;

    public DiscreteLongIntSampler(DoubleToIntFunction parentFunc, boolean hash) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
    }

    @Override
    public int applyAsInt(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        int sample =f.applyAsInt(unit);
        return sample;
    }
}

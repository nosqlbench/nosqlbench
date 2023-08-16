/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import java.util.function.LongFunction;

/**
 * A VectorSequence is a sequence of vectors which are a deterministic
 * map between a set of ordinal values and vector values. Ideally, they
 * are computed with closed-form functions. If not, they should
 * be pre-computed in cashed.
 */
public abstract class VectorSequence implements LongFunction<double[]> {

    protected final long cardinality;

    public VectorSequence(long cardinality) {
        this.cardinality = cardinality;
    }
    /**
     * @return the number of components per vector produced.
     */
    public abstract long getDimensions();

    /**
     * @return the number of unique vectors produced.
     */
    public long getCardinality() {
        return this.cardinality;
    }

    /**
     * This should return the computed minimum vector angle in radians
     * @return the minimum vector angle in radians between adjacent vectors by ordinal value
     */
    public double getMinimumVectorAngle() {
        return 0.0d;
    }
}

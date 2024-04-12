/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn.circlefield;

import io.nosqlbench.virtdata.lib.vectors.util.BitFields;

public class CFVectorSpace {

    /**
     * The size determines the cardinality of unique vectors within this space.
     * It is the maximum value exclusive, or the next value after the maximum
     * value included in the set of ordinals.
     */
    public final int size;
    /**
     * The number of bits which are needed to represent the maximum value.
     */
    public final int bits;

    /**
     * A mask which is used to limit the counting resolution to the bit size;
     */
    public final int mask;

    public CFVectorSpace(int size) {
        if (size>0x4000_0000) {
            throw new RuntimeException("Size must be less than or equal to " + 0x4000_0000 + "(0x4000_0000)" +
                ", since all values must fall at perfect divisions of 0x4000_0000");
        }
        this.size = size;
        this.bits = BitFields.getMsbPosition(size);
        this.mask = 0xFFFFFFFF >> (32-bits);
    }
    public int mask() {
        return this.mask;
    }
    public int bits() {
        return this.bits;
    }
    public int minIncluded() {
        return 0;
    }
    public int maxExcluded() {
        return 1<<30;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CFVectorSpace{");
        sb.append("size=").append(size);
        sb.append(" bits=").append(bits());
        sb.append(" [").append(minIncluded()).append(",").append(maxExcluded()).append(")");
        sb.append('}');
        return sb.toString();
    }
}

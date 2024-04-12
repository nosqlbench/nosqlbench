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

public class CFVectorMapper {
    private final CFVectorSpace space;
    private final int bits;

    public CFVectorMapper(CFVectorSpace space) {
        this.space = space;
        this.bits = space.bits();
    }

    /**
     * @param ord
     *     The ordinal value which is the stable enumeration of a vector
     * @return
     */
    public double[] vectorForOrdinal(int ord) {
        int offset = BitFields.reverseIntBitsUnsigned(ord);
        double unitFraction = unitFraction(offset, 0x4FFF_FFFF);
        return vecOnCircle(unitFraction);
    }

    private double unitFraction(int offset, int maxvalue) {
        int[] fraction = BitFields.alignReducedBits(new int[]{offset, space.maxExcluded()});
        double unitFraction = ((double) fraction[0]) / ((double) fraction[1]);
        return unitFraction;
    }

    public double[] vecOnCircle(double unit) {
        double radians = 2.0d * Math.PI * unit;
        return new double[]{Math.cos(radians), Math.sin(radians)};
    }

    public int[] neighbors(int center, int k) {
        if (center >= space.maxExcluded()) {
            throw new RuntimeException("Ordinal value " + center + " must fall within interval [" +
                space.minIncluded() + "," + space.maxExcluded() + ")" +
                " for vector space " + space.toString());
        }
        int neighborhoodSize = Math.min(space.size, k);
        if ((neighborhoodSize & 1) == 1) {
            throw new RuntimeException("neighborhood size must be an even number for now, not " + neighborhoodSize);
        }
        int[] neighborhood = new int[neighborhoodSize];

        for (int i = 0; i < neighborhood.length/2; i++) {
            int ccw = BitFields.rotateBitspace(center, i,30-(space.bits));
            neighborhood[i<<1]=BitFields.reverseIntBitsUnsigned(ccw);
            int cw = BitFields.rotateBitspace(center, -i,30-(space.bits));
            neighborhood[(i<<1)+1]=BitFields.reverseIntBitsUnsigned(cw);
        }
        return neighborhood;
    }
}

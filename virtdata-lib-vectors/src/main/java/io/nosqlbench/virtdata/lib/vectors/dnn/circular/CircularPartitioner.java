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

package io.nosqlbench.virtdata.lib.vectors.dnn.circular;

import io.nosqlbench.virtdata.lib.vectors.util.BitFields;

/**
 * <hr/>
 * <H2>Examples</H2>
 * <p>
 * One bit represents the possibility of subdividing the space in one of two ways,
 * either once at angle 0 from the fixed reference point at (1,0), or twice, at
 * the initial point and the half-way point around the unit circle.
 * </P>
 * <P>Adding a bit of resolution represents a possibility of 4 subdivisions of the
 * unit circle, and so on, doubling (subdividing) teach previous range for each bit
 * added. However, it is not strictly required that the space be divided by only
 * Some 2^n possible spaces. Any value represented by the bit field can represent a
 * number of subdivisions, whereas the actual last value known to be used determines
 * how many of the smallest intervals are subdivided and how many further are not.
 * not.</P>
 * <HR/>
 * <H2>Space Filling Curve Calculations</H2>
 * Here are the steps to mapping an ordinal to it's effective offset with the
 * representable values for a given width of register.
 * For some of these operations, you may need to mask to only retain values that properly
 * fit within the register size for the maximum value, such as the zero case.
 * <OL>
 * <LI>Level of Subdivision -Determine the strata of resolution from the most significant bit in
 * the ordinal. This is simply the leftmost bit which is set. For 0b101, the
 * MSB is 3.</LI>
 * <LI>Phase Bits - Determine the number of bits which represent all distinct points
 * at that resolution. This is calculated as one less than the msb of the ordinal.
 * For example ordinal 5 (0b101) has msb of 3, so within this level of resolution,
 * the number of significant bits is 2 (3-1) or (max msb - ordinal msb)</LI>
 * <LI>Phase Bit Shift - Determine how far the phase index image needs to shift left,
 * to align the bits at the outer most level rotation. Every strata is aligned
 * to the unit circle, only with a different initial position and step size.
 * This is calculated as 2^(max msb - ordinal msb)</LI>
 * </OL>
 */
public class CircularPartitioner {

    /**
     * <p>The number of significant binary bits in the subdivided space. There has to be
     * at least one bit, which represents the minimum divisible space. Each division
     * point which is active represents the address of a single vector from the
     * origin to the the unit circle at a position relative to the total space.
     * </p>
     */
    private final int msb;
    /**
     * The index after the last actual ordinal in the set, or in other words, the end
     * of the {@code [closed,open)} interval of ordinals.
     */
    private final int maxOrdinalExcluded;
    private final int mask;

    public CircularPartitioner(int maxOrdinalExcluded) {
        this.maxOrdinalExcluded = maxOrdinalExcluded;
        this.msb = BitFields.getMsbPosition(maxOrdinalExcluded - 1);
        this.mask = (1<<msb) - 1;
    }

    /**
     * Given the number of Return the value within the unit interval
     *
     * @param ordinal
     * @return
     */
    public double unitIntervalOf(int ordinal) {
        return ((double) ordinal) / ((double) maxOrdinalExcluded);
    }

    public int ordinalToOffset(int ordinal) {

        int ordMsb = BitFields.getMsbPosition(ordinal);
        int phaseBits = ordMsb - 1;
        int phaseMask = ((1 << phaseBits) - 1) & mask;
        int floorShift = msb - ordMsb;
        int phaseFloor = (1 << floorShift) & mask;
        int phaseIndex = ordinal & phaseMask;

        int phasePosition = phaseIndex << (msb-phaseBits);

        int value = phaseFloor + phasePosition;
        return value;
    }

    public int[] ordinalToFraction(int ordinal) {
        return new int[]{ordinalToOffset(ordinal), maxOrdinalExcluded};
    }

    public double ordinalToUnitInterval(int ordinal) {
        int remapped = ordinalToOffset(ordinal);

        return ((double) remapped) / (double) maxOrdinalExcluded;
    }

    public double[] vecOnCircle(double unit) {
        double radians = 2.0d * Math.PI * unit;
        return new double[]{Math.cos(radians), Math.sin(radians)};
    }

}

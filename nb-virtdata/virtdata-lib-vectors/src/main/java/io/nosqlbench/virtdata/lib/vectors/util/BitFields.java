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

package io.nosqlbench.virtdata.lib.vectors.util;

public class BitFields {

    /**
     * Reduce the discrete magnitude of values by the same amount,
     * to allow any IEEE floating point values to remain closer to
     * the higher-precision part of the value space. This will reduce
     * rounding error for downstream operations which are based on
     * floating point casts of integer values.
     * @param values An array of integers
     * @return A scaled-down array of integers
     */
    public static int[] alignReducedBits(int[] values) {
        int bits=32;
        for (int value : values) {
            bits=Math.min(bits,getLsbZeroBits(value));
        }
        int[] shifted = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            shifted[i]=values[i]>>bits;
        }
        return shifted;
    }

    private static final int[] zeros = {
        8, 0, 1, 0,
        2, 0, 1, 0,
        3, 0, 1, 0,
        2, 0, 1, 0
    };

    /**
     * @return The number of lower-order bits which are zero in the value
     */
    public static int getLsbZeroBits(int value) {
        int b = 0;
        for (int i = 0; i < 8; i++) {
            if ((value & 0xF)>0) {
                b+=zeros[value&0xF];
                break;
            }
            b+=4;
            value>>=4;
        }
        return b;
    }


    /**
     * @return The position of the most significant bit, with the lsb
     * represented as 1.
     */
    public static int getMsbPosition(int value) {
        if (value < 0) {
            throw new RuntimeException("Only values between 1 and " + Integer.MAX_VALUE +
                " are supported, and you tried to get the MSB position for value " + value +
                " or possible overflowed to a negative value."
            );
        }
        int r = 0;
        if ((value & 0x00000000FFFF0000L) > 0) {
            r += 16;
            value >>= 16;
        }
        if ((value & 0x000000000000FF00L) > 0) {
            r += 8;
            value >>= 8;
        }
        if ((value & 0x00000000000000F0) > 0) {
            r += 4;
            value >>= 4;
        }
        return r + msbs[(int) value];
    }
    private static final int[] msbs = {0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4};

    public static int reverseBits(int x) {
        return reverseBits2(x);
    }
    public static int reverseIntBitsUnsigned(int x) {
        return reverseBitsSave2(x);
    }

    static int reverseBits1(int x, int bits) {
        int result = 0;
        for (int i = 0; i < bits; i++) {
            result = (result << 1) | (x & 1);
            x >>= 1;
        }
        return result;
    }

    static int reverseBitsSave2(int v) {
        if (v>=0x4000_0000) {
            throw new RuntimeException("unsigned bit reversal must remain in lower 31 bits. input=" + v);
        }
        long x = (long)v;
        // Masks for bit swapping

        x = ((x >>> 1) & MASK1) | ((x & MASK1) << 1);
        x = ((x >>> 2) & MASK2) | ((x & MASK2) << 2);
        x = ((x >>> 4) & MASK3) | ((x & MASK3) << 4);
        x = ((x >>> 8) & MASK4) | ((x & MASK4) << 8);
        int signed = (int) ((x >>> 16) | (x << 16));
        return signed>>>2;
    }
    final static int MASK1 = 0x55555555; // 0b01010101010101010101010101010101
    final static int MASK2 = 0x33333333; // 0b00110011001100110011001100110011
    final static int MASK3 = 0x0F0F0F0F; // 0b00001111000011110000111100001111
    final static int MASK4 = 0x00FF00FF; // 0b00000000111111110000000011111111
    static int reverseBits2(int x) {
        // Masks for bit swapping

        x = ((x >>> 1) & MASK1) | ((x & MASK1) << 1);
        x = ((x >>> 2) & MASK2) | ((x & MASK2) << 2);
        x = ((x >>> 4) & MASK3) | ((x & MASK3) << 4);
        x = ((x >>> 8) & MASK4) | ((x & MASK4) << 8);
        return (x >>> 16) | (x << 16);
    }

    static int reverseBits3(int x) {
        return (BIT_REVERSE_TABLE[x & 0xFF] << 24) |
            (BIT_REVERSE_TABLE[(x >>> 8) & 0xFF] << 16) |
            (BIT_REVERSE_TABLE[(x >>> 16) & 0xFF] << 8) |
            BIT_REVERSE_TABLE[(x >>> 24) & 0xFF];
    }
    private static final int[] BIT_REVERSE_TABLE = buildReverseTable(8); // For byte reversal
    private static int[] buildReverseTable(int bits) {
        int size = 1 << bits;
        int[] table = new int[size];
        for (int i = 0; i < size; i++) {
            table[i] = reverseBits1(i, bits);
        }
        return table;
    }

    /**
     * Increment or decrement the initial bitfield value by some amount,
     * offset into higher 2^scale resolution.
     * @param initial The initial bitfield image
     * @param amount The value to add (positive or negative) to the initial value
     * @param shift The number of bits to shift left before, and right after the increment
     * @return The initial value incremented by the amount at some scale determined by shift
     */
    public static int rotateBitspace(int initial, int amount, int shift) {
        return ((initial<<shift)+amount)>>shift;
    }

}

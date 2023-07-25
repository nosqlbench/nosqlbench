/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl.marker.longheap;

/**
 * <p>Using the 64 bit structure of a long as a heap addressed
 * tracker, where the leaf nodes represent marked values and
 * all others are used to consolidate state.</p>
 *
 * <p>One bit is ignored, the 2s compliment sign, leaving 63 bits:
 * 31 bits for root and path and 32 bits as leaf nodes.</p>
 *
 * <p>Each time a leaf node is marked as complete, it's sibling
 * is checked for the same. While both are marked, the same
 * process is checked for its parent and its sibling, and so forth</p>
 *
 * <p>This approach assumes that it is good to lower contention and
 * retries for atomics when there are many threads active against
 * the tracker. It should be benchmarked with simpler methods
 * to see the complexity is worth it.</p>
 */
public class LongTreeTracker {

    long timage = 0L;

    private static final long odds = 0b0101010101010101010101010101010101010101010101010101010101010101L;
    private static final long eens = 0b1010101010101010101010101010101010101010101010101010101010101010L;
    private static final long msbb = 0b1000000000000000000000000000000000000000000000000000000000000000L;
    private static final long left = 0b1111111111111111111111111111111100000000000000000000000000000000L;

    public LongTreeTracker(long timage) {
        this.timage = timage;
    }

    public LongTreeTracker() {
    }

    /**
     * Apply an index value between 0 and 31 inclusive. Return the accumulator.
     * If all 32 slots of this tracker have been isCycleCompleted, the returned value will
     * have LSB bit 2 set.
     * @param index a long value between 0 and 31 to mark as complete
     * @param image the long value which serves as the starting state of the bit field
     * @return the accumulator
     */
    public long setCompleted(long index, long image) {
        long position = 63 - index;

        while (position > 0) {
            long applybt = 1L << position;
//            System.out.println("applybt:\n" + diagString(applybt));
//            System.out.print("image:\n" + this);
            image |= applybt;
            long comask = applybt | (applybt & eens) >> 1 | (applybt & odds) << 1;
//            System.out.println("comask:\n" + diagString(comask));
            if ((comask & image) != comask) {
                break;
            }
            position >>= 1;
        }
//        System.out.println("image:\n" + this);
        // TODO Fix this test
        return image;
    }

    public long setCompleted(long index) {
        return timage = setCompleted(index,timage);
    }


    public boolean isCompleted(long index) {
        long l = msbb >>> index;
        return (timage & l ) == l;
    }

    public boolean isCompleted() {
        return ((timage & 2L) > 0);
    }

    /**
     * @return the lowest index isCycleCompleted, or -1 if none were isCycleCompleted
     */
    public long getLowestCompleted() {
        int l = Long.numberOfLeadingZeros(timage&left);
        return (l!=64) ? l : -1;
    }

    /**
     * @return the highest index isCycleCompleted, or -1 if none were isCycleCompleted
     */
    public long getHighestCompleted() {
        int l = Long.numberOfTrailingZeros(timage&left);
        return (l!=64) ? 31-(l-32) : -1;
    }

    public long getTotalCompleted() {
        return Long.bitCount(timage&left);
    }

    public static String toBinaryString(long bitfield) {
        String s = Long.toBinaryString(bitfield);
        s = "0000000000000000000000000000000000000000000000000000000000000000".substring(s.length()) + s;
        return s;
    }

    public String toString() {
        return diagString(this.timage);
    }

    public static String diagString(long bitfield) {
        String s = toBinaryString(bitfield);
        String[] n = new String[64];
        for (int i = 0; i < n.length; i++) {
            n[i] = s.substring(i, i + 1);
        }

        String space = "                                                                ";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            sb.append(n[i]).append(" ");
        }
        sb.append("\n");

        sb.append(" ");
        for (int i = 32; i < 48; i++) {
            sb.append(n[i]).append("   ");
        }
        sb.append("\n");

        sb.append("   ");
        for (int i = 48; i < 56; i++) {
            sb.append(n[i]).append("       ");
        }
        sb.append("\n");

        sb.append("       ");
        for (int i = 56; i < 60; i++) {
            sb.append(n[i]).append("               ");
        }
        sb.append("\n");

        sb.append("               ");
        for (int i = 60; i < 62; i++) {
            sb.append(n[i]).append("                               ");
        }
        sb.append("\n");

        sb.append("                               ").append(n[62]).append("\n");
        sb.append("                               ").append(n[63]).append("\n");

        return sb.toString();
    }

    public long getImage() {
        return timage;
    }

}

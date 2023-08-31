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

package io.nosqlbench.engine.extensions.computefunctions;

import java.util.Arrays;

public class ComputeFunctions {

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     * @param referenceIndexes long array of indices
     * @param sampleIndexes long array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(long[] referenceIndexes, long[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes, sampleIndexes);
        return (double) intersection.length / (double) referenceIndexes.length;
    }

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     * @param referenceIndexes int array of indices
     * @param sampleIndexes int array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(int[] referenceIndexes, int[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int intersection = Intersections.count(referenceIndexes, sampleIndexes);
        return (double) intersection / (double) referenceIndexes.length;
    }

    /**
     * Compute the intersection of two long arrays
     */
    public static long[] intersection(long[] a, long[] b) {
        return Intersections.find(a,b);
    }

    /**
     * Compute the intersection of two int arrays
     */
    public static int[] intersection(int[] reference, int[] sample) {
        return Intersections.find(reference, sample);
    }

    /**
     * Compute the size of the intersection of two int arrays
     */
    public static int intersectionSize(int[] reference, int[] sample) {
        return Intersections.count(reference, sample);
    }


}

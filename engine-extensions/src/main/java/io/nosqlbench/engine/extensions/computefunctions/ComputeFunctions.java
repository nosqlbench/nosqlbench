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
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;

/**
 * <P>A collection of compute functions related to vector search relevancy.
 * These are based on arrays of indices of vectors, where the expected data is from known KNN test data,
 * and the actual data is from a vector search query.</P>
 *
 * <P>Variations of these functions have a limit parameter, which allows for derivation of relevancy
 * measurements for a smaller query without having to run a separate test for each K value.
 * If you are using test vectors from a computed KNN test data with for K=100, you can compute
 * metrics "@K" for any size up to and including K=100. This simply uses a partial view of the result
 * to do exactly what would have been done for a test where you actually query for that K limit.
 * <STRONG>This assumes that the result rank is stable irrespective of the limit AND the results
 * are passed to these functions as ranked in results.</STRONG></P> Some of the methods apply K to the
 * expected (relevant) indices, others to the actual (response) indices, depending on what is appropriate.
 *
 * <P>The array indices passed to these functions should not be sorted before-hand as a general rule.</P>
 * Yet, no provision is made for duplicate entries. If you have duplicate indices in either array,
 * these methods will yield incorrect results as they rely on the <EM>two-pointer</EM> method and do not
 * elide duplicates internally.
 */
public class ComputeFunctions {

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     *
     * @param relevant
     *     long array of indices
     * @param actual
     *     long array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(long[] relevant, long[] actual) {
        Arrays.sort(relevant);
        Arrays.sort(actual);
        long[] intersection = Intersections.find(relevant, actual);
        return (double) intersection.length / (double) relevant.length;
    }

    public static double recall(long[] relevant, long[] actual, int k) {
        if (actual.length < k) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + actual.length + ", limit=" + k);
        }
        actual = Arrays.copyOfRange(actual, 0, k);
        Arrays.sort(relevant);
        Arrays.sort(actual);
        long[] intersection = Intersections.find(relevant, actual);
        return (double) intersection.length / (double) relevant.length;
    }

    public static double precision(long[] relevant, long[] actual) {
        Arrays.sort(relevant);
        Arrays.sort(actual);
        long[] intersection = Intersections.find(relevant, actual);
        return (double) intersection.length / (double) actual.length;
    }

    public static double precision(long[] relevant, long[] actual, int k) {
        if (actual.length < k) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + actual.length + ", limit=" + k);
        }
        actual = Arrays.copyOfRange(actual, 0, k);
        Arrays.sort(relevant);
        Arrays.sort(actual);
        long[] intersection = Intersections.find(relevant, actual);
        return (double) intersection.length / (double) actual.length;
    }

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     *
     * @param relevant
     *     int array of indices
     * @param actual
     *     int array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(int[] relevant, int[] actual) {
        Arrays.sort(relevant);
        Arrays.sort(actual);
        int intersection = Intersections.count(relevant, actual);
        return (double) intersection / (double) relevant.length;
    }

    public static double recall(int[] relevant, int[] actual, int k) {
        if (actual.length < k) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + actual.length + ", limit=" + k);
        }
        actual = Arrays.copyOfRange(actual, 0, k);
        Arrays.sort(relevant);
        Arrays.sort(actual);
        int intersection = Intersections.count(relevant, actual);
        return (double) intersection / (double) relevant.length;
    }

    public static double precision(int[] relevant, int[] actual) {
        Arrays.sort(relevant);
        Arrays.sort(actual);
        int intersection = Intersections.count(relevant, actual);
        return (double) intersection / (double) actual.length;
    }

    public static double precision(int[] relevant, int[] actual, int k) {
        if (actual.length < k) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + actual.length + ", limit=" + k);
        }
        actual = Arrays.copyOfRange(actual, 0, k);
        Arrays.sort(relevant);
        Arrays.sort(actual);
        int intersection = Intersections.count(relevant, actual);
        return (double) intersection / (double) actual.length;
    }

    /**
     * Compute the intersection of two long arrays
     */
    public static long[] intersection(long[] a, long[] b) {
        return Intersections.find(a, b);
    }

    /**
     * Compute the intersection of two int arrays
     */
    public static int[] intersection(int[] a, int[] b) {
        return Intersections.find(a, b);
    }

    /**
     * Compute the size of the intersection of two int arrays
     */
    public static int intersectionSize(int[] a, int[] b) {
        return Intersections.count(a, b);
    }

    public static int intersectionSize(long[] a, long[] b) {
        return Intersections.count(a, b);
    }

    public static double F1(int[] relevant, int[] actual) {
        return F1(relevant, actual, relevant.length);
    }

    public static double F1(int[] relevant, int[] actual, int k) {
        double recallAtK = recall(relevant, actual, k);
        double precisionAtK = precision(relevant, actual, k);
        return 2.0d * ((recallAtK * precisionAtK) / (recallAtK + precisionAtK));
    }

    public static double F1(long[] relevant, long[] actual) {
        return F1(relevant, actual, relevant.length);
    }

    public static double F1(long[] relevant, long[] actual, int k) {
        double recallAtK = recall(relevant, actual, k);
        double precisionAtK = precision(relevant, actual, k);
        return 2.0d * ((recallAtK * precisionAtK) / (recallAtK + precisionAtK));
    }

    /**
     * Reciprocal Rank - The multiplicative inverse of the first rank which is relevant.
     */
    public static double reciprocal_rank(long[] relevant, long[] actual, int k) {
        int firstRank = Intersections.firstMatchingIndex(relevant, actual, k);
        if (firstRank >= 0) {
            return 1.0d / (firstRank+1);
        } else {
            return 0.0;
        }
    }

    public static double reciprocal_rank(long[] relevant, long[] actual) {
        return reciprocal_rank(relevant, actual, relevant.length);
    }

    /**
     * RR as in M(RR)
     */
    public static double reciprocal_rank(int[] relevant, int[] actual, int k) {
        int firstRank = Intersections.firstMatchingIndex(relevant, actual, k);
        if (firstRank<0) {
            return 0;
        }
        return 1.0d / (firstRank+1);
    }

    public static double reciprocal_rank(int[] relevant, int[] actual) {
        return reciprocal_rank(relevant, actual, relevant.length);
    }

    public static double average_precision(int[] relevant, int[] actual) {
        return average_precision(relevant,actual,relevant.length);
    }

    public static double average_precision(int[] relevant, int[] actual, int k) {
        int maxK = Math.min(k,actual.length);
        HashSet<Integer> relevantSet = new HashSet<>(relevant.length);
        for (Integer i : relevant) {
            relevantSet.add(i);
        }
        int relevantCount=0;
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int i = 0; i < maxK; i++) {
            if (relevantSet.contains(actual[i])){
                relevantCount++;
                double precisionAtIdx = (double) relevantCount / (i+1);
                stats.accept(precisionAtIdx);
            }
        }
        return stats.getAverage();
    }

    public static double average_precision(long[] relevant, long[] actual, int k) {
        int maxK = Math.min(k,actual.length);
        HashSet<Long> refset = new HashSet<>(relevant.length);
        for (Long i : relevant) {
            refset.add(i);
        }
        int relevantCount=0;
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int i = 0; i < maxK; i++) {
            if (refset.contains(actual[i])){
                relevantCount++;
                double precisionAtIdx = (double) relevantCount / (i+1);
                stats.accept(precisionAtIdx);
            }
        }
        return stats.getAverage();
    }

}

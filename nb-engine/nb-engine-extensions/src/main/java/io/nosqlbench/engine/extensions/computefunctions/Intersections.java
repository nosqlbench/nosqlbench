/*
 * Copyright (c) nosqlbench
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

public class Intersections {

    /**
     * Return a non-negative index of the first value in the sample array which is present in the reference array,
     * OR, return a negative number. This returns array index which start at 0, not rank, which is starts at 1.
     */
    public static int firstMatchingIndex(long[] reference, long[] sample, int limit) {
        Arrays.sort(reference);
        int maxIndex = Math.min(sample.length, limit);
        int foundAt = -1;
        for (int index = 0; index < maxIndex; index++) {
            foundAt = Arrays.binarySearch(reference, sample[index]);
            if (foundAt >= 0) return index;
        }
        return -1;
    }

    public static int firstMatchingIndex(int[] reference, int[] sample, int limit) {
        Arrays.sort(reference);
        int maxIndex = Math.min(sample.length, limit);
        int foundAt = -1;
        for (int index = 0; index < maxIndex; index++) {
            foundAt = Arrays.binarySearch(reference, sample[index]);
            if (foundAt >= 0) return index;
        }
        return -1;
    }

  /**
   Use the two-pointer method to count the number of matches between the two arrays.
   This method requires that the values be sorted in ascending order already,
   with no duplicate values.
   * @param a
   * a set of integers
   * @param b
   * a set of integers
   * @return The number of matches between the two arrays.
   */
  public static int count(int[] a, int[] b) {
    int a_index = 0, b_index = 0, matches = 0;
    int a_element, b_element;

    while (a_index < a.length && b_index < b.length) {
      a_element = a[a_index];
      b_element = b[b_index];
      if (a_element == b_element) {
        ++matches;
        a_index++;
        b_index++;
      } else if (b_element < a_element) {
        b_index++;
      } else { // a_element < b_element
        a_index++;
      }
    }
    return matches;
  }

  /**
   Use the two-pointer method to count the number of matches between the two arrays.
   This method requires that the values be sorted in ascending order already,
   with no duplicate values.
   * @param a
   * a set of integers
   * @param b
   * a set of integers
   * @return The number of matches between the two arrays.
   */
  public static int count(long[] a, long[] b) {
    int a_index = 0, b_index = 0, matches = 0;
    long a_element, b_element;

    while (a_index < a.length && b_index < b.length) {
      a_element = a[a_index];
      b_element = b[b_index];
      if (a_element == b_element) {
        ++matches;
        a_index++;
        b_index++;
      } else if (b_element < a_element) {
        b_index++;
      } else { // a_element < b_element
        a_index++;
      }
    }
    return matches;
  }

    public static int[] find(int[] a, int[] b) {
        int[] result = new int[b.length];
        int a_index = 0, b_index = 0, acc_index = -1;
        int a_element, b_element;
        while (a_index < a.length && b_index < b.length) {
            a_element = a[a_index];
            b_element = b[b_index];
            if (a_element == b_element) {
                result[++acc_index] = a_element;
                a_index++;
                b_index++;
            } else if (b_element < a_element) {
                b_index++;
            } else {
                a_index++;
            }
        }
        return Arrays.copyOfRange(result, 0, acc_index + 1);
    }

    public static long[] find(long[] reference, long[] sample) {
        long[] result = new long[sample.length];
        int a_index = 0, b_index = 0, acc_index = -1;
        long a_element, b_element;
        while (a_index < reference.length && b_index < sample.length) {
            a_element = reference[a_index];
            b_element = sample[b_index];
            if (a_element == b_element) {
                result[++acc_index] = a_element;
                a_index++;
                b_index++;
            } else if (b_element < a_element) {
                b_index++;
            } else {
                a_index++;
            }
        }
        return Arrays.copyOfRange(result, 0, acc_index + 1);
    }

}

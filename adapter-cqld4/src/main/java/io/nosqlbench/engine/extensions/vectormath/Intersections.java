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

package io.nosqlbench.engine.extensions.vectormath;

import java.util.Arrays;

public class Intersections {

    public static long[] find(long[] reference, long[] sample) {
        long[] result = new long[reference.length];
        int a_index = 0, b_index = 0, acc_index = -1;
        long a_element, b_element;
        while (a_index < reference.length && b_index < sample.length) {
            a_element = reference[a_index];
            b_element = sample[b_index];
            if (a_element == b_element) {
                result = resize(result);
                result[++acc_index] = a_element;
                a_index++;
                b_index++;
            } else if (b_element < a_element) {
                b_index++;
            } else {
                a_index++;
            }
        }
        return Arrays.copyOfRange(result,0,acc_index+1);
    }

    public static int[] find(int[] reference, int[] sample) {
        int[] result = new int[reference.length];
        int a_index = 0, b_index = 0, acc_index = -1;
        int a_element, b_element;
        while (a_index < reference.length && b_index < sample.length) {
            a_element = reference[a_index];
            b_element = sample[b_index];
            if (a_element == b_element) {
                result = resize(result);
                result[++acc_index] = a_element;
                a_index++;
                b_index++;
            } else if (b_element < a_element) {
                b_index++;
            } else {
                a_index++;
            }
        }
        return Arrays.copyOfRange(result,0,acc_index+1);
    }

    public static int[] resize(int[] arr) {
        int len = arr.length;
        int[] copy = new int[len + 1];
        for (int i = 0; i < len; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    public static long[] resize(long[] arr) {
        int len = arr.length;
        long[] copy = new long[len + 1];
        for (int i = 0; i < len; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

}

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

package io.nosqlbench.nb.api.conversions;

public class ConverterUtils {
    public static int[] toIntArray(String[] strings) {
        int[] ints = new int[strings.length];
        for (int i = 0; i < ints.length; i++) {
            ints[i]=Integer.parseInt(strings[i]);
        }
        return ints;
    }

    public static long[] toLongArray(String[] strings) {
        long[] longs = new long[strings.length];
        for (int i = 0; i < longs.length; i++) {
            longs[i]=Long.parseLong(strings[i]);
        }
        return longs;
    }
}

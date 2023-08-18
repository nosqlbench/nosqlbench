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

import com.datastax.oss.driver.api.core.cql.Row;

import java.util.Arrays;
import java.util.List;

public class VectorMath {

    public static long[] rowsToLongArray(String fieldName, List<Row> rows) {
        return rows.stream().mapToLong(r -> r.getLong(fieldName)).toArray();
    }

    public static int[] rowListToIntArray(String fieldName, List<Row> rows) {
        return rows.stream().mapToInt(r -> r.getInt(fieldName)).toArray();
    }

    public double computeRecall(long[] referenceIndexes, long[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes,sampleIndexes);
        return (double)intersection.length/(double)referenceIndexes.length;
    }

    public double computeRecall(int[] referenceIndexes, int[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int[] intersection = Intersections.find(referenceIndexes,sampleIndexes);
        return (double)intersection.length/(double)referenceIndexes.length;
    }

}

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

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;

import java.util.Arrays;

public class PineconeVectorMath {

    public static long[] stringArrayAsALongArray(String[] strings) {
        long[] longs = new long[strings.length];
        for (int i = 0; i < longs.length; i++) {
            longs[i]=Long.parseLong(strings[i]);
        }
        return longs;
    }

    public static int[] stringArrayAsIntArray(String[] strings) {
        int[] ints = new int[strings.length];
        for (int i = 0; i < ints.length; i++) {
            ints[i]=Integer.parseInt(strings[i]);
        }
        return ints;
    }

    public static String[] idsToStringArray(QueryResponse response) {
        return response.getMatchesList().stream().map(ScoredVector::getId).toArray(String[]::new);
    }

    public static int[] idsToIntArray(QueryResponse response) {
        return response.getMatchesList().stream().mapToInt(r -> Integer.parseInt(r.getId())).toArray();
    }

    public static double computeRecall(long[] referenceIndexes, long[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = PineconeIntersections.find(referenceIndexes,sampleIndexes);
        return (double)intersection.length/(double)referenceIndexes.length;
    }

    public static double computeRecall(int[] referenceIndexes, int[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int[] intersection = PineconeIntersections.find(referenceIndexes,sampleIndexes);
        return (double)intersection.length/(double)referenceIndexes.length;
    }

}

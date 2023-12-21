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

package io.nosqlbench.virtdata.library.basics.shared.vectors.dnn;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DNNAngular1VTest {

    @Test
    public void testCosineSimilarity() {
        assertThat(cosine_similarity(new float[]{1, 2, 3, 4, 5, 6, 7}, new float[]{7, 6, 5, 4, 3, 2, 1})).isEqualTo(0.6);
        assertThat(cosine_similarity(new float[]{1, 2, 3, 4, 5, 6, 7}, new float[]{1, 2, 3, 4, 5, 6, 7})).isEqualTo(1.0);
    }

    @Test
    public void testSimpleGeneration() {
        DNN_angular1_v vs = new DNN_angular1_v(2, 100, 3);
        assertThat(vs.apply(0)).isEqualTo(new float[]{1, 0});
        assertThat(vs.apply(1)).isEqualTo(new float[]{2, 2});
        assertThat(vs.apply(2)).isEqualTo(new float[]{3, 6});
        assertThat(vs.apply(3)).isEqualTo(new float[]{4, 0});
        assertThat(vs.apply(4)).isEqualTo(new float[]{5, 5});
        assertThat(vs.apply(5)).isEqualTo(new float[]{6, 12});
        assertThat(vs.apply(6)).isEqualTo(new float[]{7, 0});
    }

    @Test
    public void testBasicAngularVectors() {
        int M = 7;
        DNN_angular1_v vf = new DNN_angular1_v(10, 100, M);
        float[][] vectors = new float[100][];
        for (int i = 0; i < 100; i++) {
            vectors[i] = vf.apply(i);
        }
        int[] same = new int[100];
        Arrays.fill(same, -1);
        for (int vidx = 0; vidx < same.length; vidx++) {
            for (int compare_to = 0; compare_to <= vidx; compare_to++) {
                double similarity = cosine_similarity(vectors[vidx], vectors[compare_to]);
                if (Math.abs(similarity - 1.0d) < 0.00000001d) {
                    same[vidx] = compare_to;
                    break;
                }
            }
        }
        for (int sameas = M; sameas < same.length; sameas++) {
//            System.out.println("idx:" + sameas + ", same[sameas] -> " + same[sameas] + " sameas%7=" + sameas % M);
            assertThat(same[sameas] % M).isEqualTo(sameas % M);
        }
    }

    private double cosine_similarity(float[] a, float[] b) {
        double dp = 0.0d;
        double as = 0.0d;
        double bs = 0.0d;
        for (int i = 0; i < a.length; i++) {
            dp += (a[i] * b[i]);
            as += (a[i] * a[i]);
            bs += (b[i] * b[i]);
        }
        return dp / (Math.sqrt(as) * Math.sqrt(bs));
    }

}

/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn.angular;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DNNAngular1NeighborsTest {

    @Test
    public void test_DNN_modulus_divides_training_population() {
        int k = 3;
        int N = 30;
        int modulus = 5;
        DNN_angular1_neighbors idxF = new DNN_angular1_neighbors(k, N, modulus);

        // NOTE: we get more than k neighbors (N / modulus, precisely), due to not arbitrarily breaking ties
        assertThat(idxF.apply(0)).isEqualTo(new int[]{0,5,10,15,20,25});
        assertThat(idxF.apply(1)).isEqualTo(new int[]{1,6,11,16,21,26});
        assertThat(idxF.apply(2)).isEqualTo(new int[]{2,7,12,17,22,27});
        assertThat(idxF.apply(3)).isEqualTo(new int[]{3,8,13,18,23,28});
        assertThat(idxF.apply(4)).isEqualTo(new int[]{4,9,14,19,24,29});

        // verify we cycle back neighbors
        for (int i = 1000; i < 1000 + modulus; i++) {
            assertThat(idxF.apply(i)).isEqualTo(idxF.apply(i % modulus));
        }
    }

    @Test
    public void test_DNN_modulus_does_not_divide_training_population() {
        int k = 3;
        int N = 30;
        int modulus = 7;
        DNN_angular1_neighbors idxF = new DNN_angular1_neighbors(k, N, modulus);

        // residue classes < N % modulus get an extra neighbor in their cluster
        assertThat(idxF.apply(0)).isEqualTo(new int[]{0,7,14,21,28});
        assertThat(idxF.apply(1)).isEqualTo(new int[]{1,8,15,22,29});
        assertThat(idxF.apply(2)).isEqualTo(new int[]{2,9,16,23});
        assertThat(idxF.apply(3)).isEqualTo(new int[]{3,10,17,24});
        assertThat(idxF.apply(4)).isEqualTo(new int[]{4,11,18,25});
        assertThat(idxF.apply(5)).isEqualTo(new int[]{5,12,19,26});
        assertThat(idxF.apply(6)).isEqualTo(new int[]{6,13,20,27});

        // verify we cycle back neighbors
        for (int i = 1000; i < 1000 + modulus; i++) {
            assertThat(idxF.apply(i)).isEqualTo(idxF.apply(i % modulus));
        }
    }
}

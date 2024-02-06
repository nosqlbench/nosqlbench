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

package io.nosqlbench.virtdata.lib.vectors.dnn.euclidean;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DNNEuclideanNeighborsTest {

    @Test
    public void test_DNN_K3_N7_D5() {
        DNN_euclidean_neighbors idxF = new DNN_euclidean_neighbors(3, 7, 5);
        assertThat(idxF.apply(0)).isEqualTo(new int[]{0,1,2});
        assertThat(idxF.apply(1)).isEqualTo(new int[]{1,0,2});
        assertThat(idxF.apply(2)).isEqualTo(new int[]{2,1,3});
        assertThat(idxF.apply(3)).isEqualTo(new int[]{3,2,4});
        assertThat(idxF.apply(4)).isEqualTo(new int[]{4,3,5});
        assertThat(idxF.apply(5)).isEqualTo(new int[]{5,4,6});
        assertThat(idxF.apply(6)).isEqualTo(new int[]{6,5,4});
    }

    @Test
    public void test_DNN_k4_n7_d5() {
        DNN_euclidean_neighbors idxF = new DNN_euclidean_neighbors(4, 7, 5);
        assertThat(idxF.apply(0)).isEqualTo(new int[]{0,1,2,3});
        assertThat(idxF.apply(1)).isEqualTo(new int[]{1,0,2,3});
        assertThat(idxF.apply(2)).isEqualTo(new int[]{2,1,3,0});
        assertThat(idxF.apply(3)).isEqualTo(new int[]{3,2,4,1});
        assertThat(idxF.apply(4)).isEqualTo(new int[]{4,3,5,2});
        assertThat(idxF.apply(5)).isEqualTo(new int[]{5,4,6,3});
        assertThat(idxF.apply(6)).isEqualTo(new int[]{6,5,4,3});
    }

    @Test
    public void test_DNN_k6_n100_d10() {
        DNN_euclidean_neighbors idxF = new DNN_euclidean_neighbors(6, 100, 10);
        assertThat(idxF.apply(99)).isEqualTo(new int[]{99,98,97,96,95,94});
    }

    @Test
    public void test_DNN_K6_N101_D10() {
        DNN_euclidean_neighbors idxF = new DNN_euclidean_neighbors(6, 101, 10);
        assertThat(idxF.apply(101)).isEqualTo(new int[]{100,99,98,97,96,95});
        assertThat(idxF.apply(100)).isEqualTo(new int[]{100,99,98,97,96,95});
        assertThat(idxF.apply(99)).isEqualTo(new int[]{99,98,100,97,96,95});
        assertThat(idxF.apply(98)).isEqualTo(new int[]{98,97,99,96,100,95});
    }

}
